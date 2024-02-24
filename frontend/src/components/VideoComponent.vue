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
                <a-popover>
                    <template #title>
                        <span>Video Information & Explanation</span>
                    </template>
                    <template #content>
                        <div>
                            <h4>Video Information</h4>
                            <!-- <a-form model="explanation">
                                <a-form-item
                                    v-for="(value, key) in videoInfo"
                                    :key="key"
                                    :label="key.charAt(0).toUpperCase() + key.slice(1)">
                                    <a-input :value="value" disabled></a-input>
                                </a-form-item>
                            </a-form> -->
                        </div>
                        <div>
                            <h4>Explanation</h4>
                            <p>{{ explanation }}</p>
                        </div>
                    </template>
                    <a-button shape="circle" size="large">
                        <template #icon>
                            <InfoCircleFilled />
                        </template>
                    </a-button>
                </a-popover>

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
            </a-space-compact>
        </div>
    </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, onMounted, onUnmounted, nextTick, defineExpose } from 'vue';
import { InfoCircleFilled, LikeTwoTone, DislikeTwoTone } from '@ant-design/icons-vue';
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


// feedback sent to the server when the video is ended
let feedback = {
    videoId: props.videoInfo.id,
    rating: -1, // -1 means no feedback
    totalWatchTime: 0, // in seconds
    interaction: new Array(),
};

const overlayStyle = ref({
    width: '480x',
    height: '680px',
});

const recordInteraction = (type) => {
    const currentTime = youtubePlayer.value?.player.getCurrentTime() || 0;

    feedback.interaction.push({
        type,
        time: currentTime
    });

    // console.log("record interaction: ", type);
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


const submitFeedback = () => {
    feedback.totalWatchTime = youtubePlayer.value?.player.getCurrentTime();
    if (feedback.totalWatchTime >= 1) {
        console.log("submit feedback for video: ", feedback);
        apiClient.submitFeedback(feedback);
    }
}

const setLikeFeedback = async () => {
    if (!globalState.userId) {
        ElMessage.error('Please login first');
        return false;
    }
    feedback.rating = 5;

    likeClicked.value = !likeClicked.value;
    if (dislikeClicked.value) dislikeClicked.value = false;

};

const setDislikeFeedback = async () => {
    if (!globalState.userId) {
        ElMessage.error('Please login first');
        return;
    }
    feedback.rating = 0;

    dislikeClicked.value = !dislikeClicked.value;
    if (likeClicked.value) likeClicked.value = false;
};

const scrollIntoView = () => {
    videoContainer.value.scrollIntoView({ behavior: 'smooth', block: 'center' });
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
    z-index: 2;
    /* background-color: rgba(0, 0, 0, 0.5); */
}
</style>
