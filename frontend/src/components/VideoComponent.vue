<template>
    <div class="video-component" ref="videoContainer">

        <!-- Youtube video -->

        <YoutubeVue3 ref="youtubePlayer" :videoid="videoInfo.id" @ended="onEnded" @paused="onPaused" @played="onPlayed"
            autoplay="0" controls=1 :height="'100%'" scrolling="no"
            style="border-radius: 10px; box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1); overflow-y: hidden;" />
        <div class="overlay" 
         :style="overlayStyle.value"
         @wheel.prevent="handleWheel" 
        >
        </div>


        <!-- Side button -->
        <div class="video-button-list">
            <a-space-compact direction="vertical" size="large">

                
                <!-- Info Button -->
                <a-button @click="showInfoModal = true" shape="circle" size="large">
                    <template #icon>
                        <InfoCircleFilled />
                    </template>
                </a-button>
                
                <!-- Like and Dislike Button -->
                <a-button shape="circle" size="large" @click="setLikeFeedback">
                    <template #icon>
                        <LikeTwoTone :two-tone-color="likeClicked ? '#f5222d' : '#000000'" />
                    </template>
                </a-button>
                <a-button shape="circle" size="large" @click="setDislikeFeedback">
                    <template #icon>
                        <DislikeTwoTone :two-tone-color="dislikeClicked ? '#52c41a' : '#000000'" />
                    </template>
                </a-button>

                <!-- Rating Button -->
                <a-button @click="showRatingModal = true" shape="circle" size="large">
                        <template #icon>
                            <EllipsisOutlined />
                        </template>
                </a-button> 
               
            </a-space-compact>

            <!-- Info Modal -->
            <a-modal v-model:visible="showInfoModal" :title="videoInfo.snippet.title" :footer="null">
                <div>
                    <!-- Display only the first 5 tags -->
                    <div class="tags-container" v-if="videoInfo.snippet.tags">
                        <a-tag v-for="(tag, index) in videoInfo.snippet.tags.slice(0, 5)" :key="index" :color="getRandomColor(index)">
                        {{ tag }}
                        </a-tag>
                    </div>
                    <a-typography-paragraph>
                        <blockquote v-if="isCollapsed" @click="toggleCollapse">
                        {{ truncatedDescription }}
                        <span v-if="isLong" style="font-weight: bold; color: rgb(59, 151, 167);">...(click to see more)</span>
                        </blockquote>
                        <blockquote v-else @click="toggleCollapse">
                        {{ videoInfo.snippet.description }}
                        </blockquote>
                    </a-typography-paragraph>
            
                    <div class="explanation-container">
                        <h3>Why this video is recommended:</h3>
                        <p>{{ explanation }}</p>
                    </div>
                </div>
            </a-modal>

            <!-- Rating Modal -->
            <a-modal v-model:visible="showRatingModal" title="Rate" okText="Submit" cancelText="Cancel" @ok="onSubmitRatingModal">
                <div class="rate-container">
                    <a-rate v-model:value="rating" />
                </div>
                <div class="not-recommend-text">I do not wish to recommend similar videos</div>
                <a-button-group class="options-group">
                    <a-button>Option 1</a-button>
                    <a-button>Option 2</a-button>
                    <a-button>Option 3</a-button>
                </a-button-group>
            </a-modal>

        </div>
    </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, onMounted, onUnmounted, nextTick, defineExpose, computed} from 'vue';
import { InfoCircleFilled, LikeTwoTone, DislikeTwoTone,EllipsisOutlined } from '@ant-design/icons-vue';
import apiClient from '@/config/apiClient';
import globalState from '@/config/globalState';
import { YoutubeVue3 } from 'youtube-vue3'
import { ElMessage } from 'element-plus';


const props = defineProps({
    videoInfo: Object,
    explanation: String,
});

const emit = defineEmits(['videoEnded', 'updateIndex']);
const likeClicked = ref(false);
const dislikeClicked = ref(false);
const youtubePlayer = ref(null);
const videoContainer = ref(null);

const showRatingModal = ref(false);
const showInfoModal = ref(false);
const predefinedColors = ['magenta', 'red', 'volcano', 'orange', 'gold', 'lime', 'green', 'cyan', 'blue', 'geekblue', 'purple'];
var colors = ref([]);

// feedback sent to the server when the video is ended
let feedback = {
    videoId: props.videoInfo.id,
    rating: 0, // 0 means no feedback
    totalWatchTime: 0, // in seconds
    interactions: new Array(),
};

const overlayStyle = ref({
    width: '480x',
    height: '680px',
});

const recordInteraction = async (type) => {
    const currentTime = await youtubePlayer.value?.player.getCurrentTime() || 0;

    feedback.interactions.push({
        type,
        time: currentTime
    });

    // console.log("record interaction: ", props.videoInfo.snippet.title+" "+type);
}

function onPaused() {
    // feedback.totalWatchTime += Date.now() - watchStartTime;
    recordInteraction('paused');
}

function onPlayed() {
    // watchStartTime = Date.now();
    recordInteraction('played');
}

function onEnded() {
    // feedback.totalWatchTime += Date.now() - watchStartTime;
    emit('videoEnded');
    recordInteraction('ended');
}


const submitFeedback = async () => {
    feedback.totalWatchTime = await youtubePlayer.value?.player.getCurrentTime();
    
    if (feedback.totalWatchTime >= 1) {  // longer than 1 second
        // console.log("Cashed feedback for video: ", feedback);
        apiClient.submitFeedback(feedback);
    }
}

const setLikeFeedback = async () => {
    if (!globalState.userId) {
        ElMessage.error('Please login first');
        return false;
    }
    if(likeClicked.value) { // if already clicked, then cancel the feedback
        feedback.rating = 0;
        rating.value = 0;
    } else {
        feedback.rating = 5;
        rating.value = 5;
    }
    
    likeClicked.value = !likeClicked.value;
    if (dislikeClicked.value) dislikeClicked.value = false;

};

const setDislikeFeedback = async () => {
    if (!globalState.userId) {
        ElMessage.error('Please login first');
        return;
    }
    if(dislikeClicked.value) { // if already clicked, then cancel the feedback
        feedback.rating = 0;
        rating.value = 0;
    } else {
        feedback.rating = 1;
        rating.value = 1;
    }

    dislikeClicked.value = !dislikeClicked.value;
    if (likeClicked.value) likeClicked.value = false;
};

const scrollIntoView = async () => {
    if (showInfoModal.value && !isCollapsed.value) {
        return false;
    }
    videoContainer.value.scrollIntoView({ behavior: 'smooth', block: 'center' });
    await new Promise(resolve => setTimeout(resolve, 500)); 
    return true;
}

const handleWheel = (event) => {
    event.preventDefault();
    // scroll the window with reverse direction
    const scrollAmount = event.deltaY > 0 ? -100 : 100;
    window.scrollBy({ top: scrollAmount, behavior: 'auto' });
};

// eslint-disable-next-line
async function updateOverlayStyle() {
    const iframe = await youtubePlayer.value.player.getIframe();
    var height = iframe.clientWidth;
    var width = iframe.clientWidth;
    
    
    height = height || 680; 
    width = width || 480;
    
    const overlayHeight = height * 0.75; 
    overlayStyle.value.height = overlayHeight + 'px';
    overlayStyle.value.width = width + 'px';
    console.log("update overlay style: ", overlayStyle.value);
    
}

defineExpose({
    submitFeedback, recordInteraction,
    scrollIntoView
})

let observer;

onMounted(() => {
   
    nextTick(() => {
        observer = new IntersectionObserver(
            ([entry]) => {
                if (entry.intersectionRatio > 0.5) { // when 50% of the video is visible

                    youtubePlayer.value.player.playVideo();
                    youtubePlayer.value.player.unMute();


                    // scroll to the center of the video automatically
                    // entry.target.scrollIntoView({
                    //     behavior: 'smooth',
                    //     block: 'center'
                    // });

                    emit('updateIndex');


                } else {
                    youtubePlayer.value.player.pauseVideo();

                }
            },
            { threshold: [0.5] }
        );

        if (videoContainer.value) {
            observer.observe(videoContainer.value);
        }
        
    });

    // updateOverlayStyle();

});



onUnmounted(() => { 
    observer.disconnect();

});


function getRandomColor(index) {
    if (colors.value[index]) {
        return colors.value[index];
    }
    var tag= props.videoInfo.snippet.tags[index];
    var sum = 0;
    for (let i = 0; i < tag.length; i++) {
        sum += tag.charCodeAt(i); // Sum the ASCII values of the characters in the text
    }
    const i = sum % predefinedColors.length; // Use the sum to find an index within the colors array
    colors.value[index] = predefinedColors[i];
    return predefinedColors[i];
}

// collapse the description in the modal
const isCollapsed = ref(true);
const maxLength = 200; // Maximum characters to show before truncation
const isLong = computed(() => props.videoInfo.snippet.description.length > maxLength);
const toggleCollapse = () => {
    isCollapsed.value = !isCollapsed.value;
};

// truncate the description
const truncatedDescription = computed(() => {
    if (props.videoInfo.snippet.description.length > maxLength) {
        return props.videoInfo.snippet.description.substring(0, maxLength);
    }
    return props.videoInfo.snippet.description;
});

// Rating modal
const rating = ref(0);
const onSubmitRatingModal = () => {
    showRatingModal.value = false;
    feedback.rating = rating.value;
    ElMessage.success('Thank you for your feedback!');
};

</script>

<style scoped>
.video-component {
    display: flex;
    justify-content: center;
    align-items: center;
    height: 85vh;
    margin: 0 auto;
    max-width: 90%;
    position: relative;
}


.video-button-list {
    display: flex;
    flex-direction: column;
    justify-content: flex-end;
    align-items: center;
    width: 5%;
    height: 100%;
    padding-bottom: 20px;
    margin-left: 10px;
    z-index: 3;
}

.a-space {
    margin-bottom: 60px;
    font-size: 20px;
}

.overlay {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 92.5%;
    /* z-index: 2; */
    /* background-color: rgba(0, 0, 0, 0.5); */
}

.tags-container {
  margin-bottom: 16px; /* Adds space below the entire tags container */
  display: flex; /* Enables flexbox layout for the tags */
  flex-wrap: wrap; /* Allows tags to wrap onto multiple lines */
  gap: 8px 4px; /* Creates a gap between tags and between lines */
}

.explanation-container h3 {
  margin-top: 0; /* Removes the top margin from the heading if needed */
  margin-bottom: 8px; /* Adds some space above the explanation text */
}

.explanation-container p {
  margin-top: 10; /* Adjusts spacing as needed */
}

.rate-container {
    margin-bottom: 20px; /* Adds space below the rating component */
}

.not-recommend-text {
    margin-top: 20px; /* Adds space above the text */
    margin-bottom: 10px; /* Adds space below the text for separation from options */
    font-weight: bold; /* Makes the text bold */
}

</style>
