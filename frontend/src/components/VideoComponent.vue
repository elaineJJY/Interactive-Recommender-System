<template>
    <div class="video-component" ref="videoContainer">

        <!-- Youtube video -->

        <YoutubeVue3 ref="youtubePlayer" :videoid="videoInfo.id" @ended="onEnded" @paused="onPaused" @played="onPlayed"
            autoplay="0" controls=1 :height="'100%'" scrolling="no"
            style="border-radius: 10px; box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1); overflow-y: hidden;" />
        <div class="overlay" :style="overlayStyle.value" @wheel.prevent="handleWheel">
        </div>


        <!-- Side button -->
        <div class="video-button-list">
            <a-space-compact direction="vertical" size="large">


                <!-- Info Button -->
                <a-button @click="showInfoModal = true; saveInteraction('Video: Info button')" shape="circle"
                    size="large" ref="infoButtonRef">
                    <template #icon>
                        <InfoCircleFilled />
                    </template>
                </a-button>

                <!-- Like and Dislike Button -->
                
                    <a-button shape="circle" size="large" @click="setLikeFeedback(); saveInteraction('Like button')"
                        ref="likeButtonRef">
                        <template #icon>
                            <LikeTwoTone :two-tone-color="likeClicked ? '#52c41a' : '#000000'" />
                        </template>
                    </a-button>
                    <a-button shape="circle" size="large"
                        @click="setDislikeFeedback(); saveInteraction('Dislike button')">
                        <template #icon>
                            <DislikeTwoTone :two-tone-color="dislikeClicked ? '#f5222d' : '#000000'" />
                        </template>
                    </a-button>
                

                <!-- More Button -->
                <a-button @click="showRatingModal = true; saveInteraction('\'...\' button', 'Open')" shape="circle"
                    size="large" ref="moreButtonRef">
                    <template #icon>
                        <EllipsisOutlined />
                    </template>
                </a-button>

            </a-space-compact>

            <!-- Info Modal -->
            <a-modal v-model:visible="showInfoModal" :title="videoInfo.snippet.title" :footer="null">
                <div class="description-container">

                    <div class="tags-container" v-if="topics">

                        <a-popover v-for="(topic, index) in topics" :key="index" placement="top" trigger="hover">
                            <template #content>
                                <div>
                                    <p>
                                        <b>{{ topic.description }}</b>
                                    </p>
                                    <img class="topic-image"
                                        :src="require(`@/assets/topics/topic_${topic.id}_wordcloud.png`)"
                                        alt="Topic image">
                                    <a-space>
                                        <a-button size="small" class="see-more-btn"
                                            :class="{ 'see-more-btn-active': selectedMoreTopicGroups.includes(topic.id) }"
                                            @click="adaptTopicGroup(index, 1); saveInteraction('\'See more\' button', topic.id)">See
                                            more?</a-button>
                                        <a-button size="small" class="see-less-btn"
                                            :class="{ 'see-less-btn-active': selectedLessTopicGroups.includes(topic.id) }"
                                            @click="adaptTopicGroup(index, -1); saveInteraction('\'See less\' button', topic.id)">See
                                            less?</a-button>
                                    </a-space>
                                </div>
                            </template>
                            <a-tag :color="getColor(topic.id)" class="custom-tag">
                                {{ topic.description }}
                            </a-tag>
                        </a-popover>
                    </div>
                    <a-typography-paragraph>
                        <blockquote v-if="isCollapsed" @click="toggleCollapse">
                            {{ truncatedDescription }}
                            <span v-if="isLong" style="font-weight: bold; color: rgb(59, 151, 167);">...(click to see
                                more)</span>
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
            <a-modal v-model:visible="showRatingModal" title="Rate" okText="Submit" cancelText="Cancel"
                @ok="onSubmitRatingModal();" @cancel="saveInteraction('\'...\' button', 'Cancel')" :width="400">
                <div class="rate-container">
                    <p style="color:gray; margin-bottom: -2px;">Your rating helps us deliver more relevant content.</p>
                    <a-rate v-model:value="rating" />
                    <span v-if="rating > 0" style="margin-left: 16px;">Score: {{ rating }}</span>
                    <span v-else style="margin-left: 16px;">You have not rated yet.</span>
                </div>
                <div class="not-recommend-text">I don't want to see it.</div>
                <p style="color:gray">We will reduce the recommendation of related videos based on the reasons you give
                    us
                </p>
                <a-space direction="vertical">
                    <a-button v-for="(option, index) in options" :key="index"
                        :class="{ 'selected-option': selectedOptions.includes(option.value), 'hover-option': !selectedOptions.includes(option.value) }"
                        @click="toggleOption(option.value)">
                        {{ option.label }}
                    </a-button>
                </a-space>
            </a-modal>

        </div>
        <a-tour v-model:current="current" :open="open" :steps="steps" @close="handleTourFinish" />
    </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, onMounted, onUnmounted, nextTick, defineExpose, computed,h, createVNode } from 'vue';
import { InfoCircleFilled, LikeTwoTone, DislikeTwoTone, EllipsisOutlined, PlusSquareOutlined, MinusSquareFilled} from '@ant-design/icons-vue';
import apiClient from '@/config/apiClient';
import globalState from '@/config/globalState';
import { YoutubeVue3 } from 'youtube-vue3'
import { ElMessage } from 'element-plus';
import { message, Tag } from 'ant-design-vue';


const props = defineProps({
    videoInfo: Object,
    explanation: String,
    topics: Array,
    videoIndex: Number,
    round: Number,
});

const emit = defineEmits(['videoEnded', 'updateIndex']);
const likeClicked = ref(false);
const dislikeClicked = ref(false);
const youtubePlayer = ref(null);
const videoContainer = ref(null);

const showRatingModal = ref(false);
const showInfoModal = ref(false);
const predefinedColors = ['magenta', 'red', 'volcano', 'orange', 'gold', 'lime', 'green', 'cyan', 'blue', 'geekblue', 'purple'];

// feedback sent to the server when the video is ended
let feedback = {
    videoId: props.videoInfo.id,
    rating: 0, // 0 means no feedback
    totalWatchTime: 0, // in seconds
    interactions: new Array(),
    dislikeReasons: new Array(),
    more: new Array(),
    less: new Array(),
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
const saveInteraction = (component, value = "") => {
    const interaction = {
        page: 'Video',
        videoId: props.videoInfo.id,
        component: component,
        componentValue: value,
    };
    apiClient.submitInteraction(interaction);
};

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
        console.log("Cashed feedback for video: ", feedback);
        feedback.less = selectedLessTopicGroups.value;
        feedback.more = selectedMoreTopicGroups.value;
        apiClient.submitFeedback(feedback);
    }
}

const setLikeFeedback = async () => {
    if (!globalState.userId) {
        ElMessage.error('Please login first');
        return false;
    }
    if (likeClicked.value) { // if already clicked, then cancel the feedback
        feedback.rating = 0;
        rating.value = 0;
    } else {
        feedback.rating = 5;
        rating.value = 5;
        recordInteraction('like');
    }

    likeClicked.value = !likeClicked.value;
    if (dislikeClicked.value) dislikeClicked.value = false;

};

const setDislikeFeedback = async () => {
    if (!globalState.userId) {
        ElMessage.error('Please login first');
        return;
    }
    if (dislikeClicked.value) { // if already clicked, then cancel the feedback
        feedback.rating = 0;
        rating.value = 0;
    } else {
        feedback.rating = 1;
        rating.value = 1;
        recordInteraction('dislike');
    }

    dislikeClicked.value = !dislikeClicked.value;
    if (likeClicked.value) likeClicked.value = false;
};

const scrollIntoView = async () => {
    if (showInfoModal.value) {
        return false;
    }
    videoContainer.value.scrollIntoView({ behavior: 'smooth', block: 'center' });
    await new Promise(resolve => setTimeout(resolve, 700));
    if(props.videoIndex === 2 && props.round === 0){
        userProfileRef = document.querySelector('.user-profile-button')
        console.log("userProfileRef: ", userProfileRef);
        open.value = true;
    }
    else {
        youtubePlayer.value.player.playVideo();
        youtubePlayer.value.player.unMute();
        emit('updateIndex');
    }
    return true;
}

const handleWheel = (event) => {
    if (showInfoModal.value) {
        return;
    }
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

// Tour
const infoButtonRef = ref(null);
const likeButtonRef = ref(null);
const moreButtonRef = ref(null);
let userProfileRef = ref(null);
const current = ref(0);
const open = ref(false);
const steps = ref([
    {
        target: () => infoButtonRef.value && infoButtonRef.value.$el,
        title: 'Info Button',
        description: 'Click here to view more information about the video and modify the tags.',
        cover: createVNode('img', {
            alt: 'tour.png',
            src: require(`@/assets/tour/infoButton.png`),
        }),
    },
    {
        target: () => likeButtonRef.value && likeButtonRef.value.$el,
        title: 'Like or Dislike',
        description: 'You can express your liking or disliking of the video here.',
    },
    {
        target: () => moreButtonRef.value && moreButtonRef.value.$el,
        title: 'More Button',
        description: 'Click here to provide a more detailed evaluation of the video, including specific reasons for disliking it.',
        cover: createVNode('img', {
            alt: 'tour.png',
            src: require(`@/assets/tour/moreButton.png`),
        }),
    },
    {
        target: null,//() =>userProfileRef.value && userProfileRef.value.$el,
        title: 'User Profile',
        description: 'Click here to view your user profile and see your viewing history.',
        cover: createVNode('img', {
            alt: 'tour.png',
            src: require(`@/assets/tour/userProfile.png`),
        }),
    },
]);


const handleTourFinish = () => {
    open.value = false;
    saveInteraction('Tour', 'Finish');
    youtubePlayer.value.player.playVideo();
    youtubePlayer.value.player.unMute();
    emit('updateIndex');
};

onMounted(() => {

    nextTick(() => {
        observer = new IntersectionObserver(
            ([entry]) => {
                if (entry.intersectionRatio <= 0.5) { 
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
    submitFeedback();
    observer.disconnect();
});



function getColor(id) {
    return predefinedColors[id % predefinedColors.length];
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


const selectedMoreTopicGroups = ref([]);
const selectedLessTopicGroups = ref([]);

// send the feedback to the topic group, 1 means want to see more, -1 means less
const adaptTopicGroup = (index, direction) => {
    var topic = props.topics[index];
    recordInteraction('adaptTopicGroup: topicId=' + topic.id + ' ' + direction);
    var info;
    if (direction == 1) {
        selectedLessTopicGroups.value = selectedLessTopicGroups.value.filter(item => item !== topic.id);
        if (!selectedMoreTopicGroups.value.includes(topic.id)) {
            selectedMoreTopicGroups.value.push(topic.id);
            info = "You wii see more videos about the topic group: ";
        }
        else {
            selectedMoreTopicGroups.value = selectedMoreTopicGroups.value.filter(item => item !== topic.id);
            info = "You have canceled your selection about the topic group: ";
        }
        
    } else {
        selectedMoreTopicGroups.value = selectedMoreTopicGroups.value.filter(item => item !== topic.id);
        if (!selectedLessTopicGroups.value.includes(topic.id)) {
            selectedLessTopicGroups.value.push(topic.id);
            info = "You wii see less videos about the topic group: ";
        }
        else {
            selectedLessTopicGroups.value = selectedLessTopicGroups.value.filter(item => item !== topic.id);
            info = "You have canceled your selection about the topic group: ";
        }
    }

    var color = direction == 1 ? '#3aaf00' : 'rgb(216, 135, 135)';
     var icon = direction === 1
        ? h(PlusSquareOutlined, { style: { color: '#3aaf00' } })
        : h(MinusSquareFilled, { style: { color: 'rgb(216, 135, 135)' } });
    
    message.open({
        content: h('div', null, [
            icon,
            h('span', { style: { marginLeft: '4px' } }, [
                h('span', { style: { color: color } },  info),
                h(Tag, {
                     color: getColor(topic.id),
                    class: 'custom-tag' 
                }, () => topic.description)
            ])
        ]),
        icon: null,
        duration: 3,
    });
};

// Rating modal
const rating = ref(0);

// save the rating and options to the feedback
const onSubmitRatingModal = () => {

    showRatingModal.value = false;
    ElMessage.success('Thank you for your feedback!');
    
    feedback.rating = rating.value;
    if (rating.value > 0) {
        recordInteraction('rating: ' + rating.value);
    }
    
    // check if the options are changed and record the interaction & feedback
    var optionIsChanged =  !(feedback.dislikeReasons.length === selectedOptions.value.length && feedback.dislikeReasons.every((value, index) => value === selectedOptions.value[index]));
    if (optionIsChanged){
        recordInteraction('dislikeReasons: ' + selectedOptions.value);
        feedback.dislikeReasons = selectedOptions.value;
    }
    let value = {};
    value.dislikeReasons = selectedOptions.value;
    value.rating = rating.value;
    saveInteraction('\'...\' button',value);
    
};

const options = [
    { value: 'Too much similar content', label: 'Too much similar content' },
    { value: 'Not interested in this content', label: 'Not interested in this content' },
    { value: 'Dislike the creator', label: 'Dislike the creator' + (props.videoInfo ? ": "+props.videoInfo.snippet.channelTitle : "") }
];

const selectedOptions = ref([]);
const toggleOption = (option) => {
    const index = selectedOptions.value.indexOf(option);
    if (index === -1) {
        selectedOptions.value.push(option);
    } else {
        selectedOptions.value.splice(index, 1);
    }
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
    margin-left: 15px;
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

.description-container {
    scrollbar-width: thin;
    -ms-overflow-style: thin;
    max-height: 500px;
    overflow-y: auto;
}

.tags-container {
    margin: 5px;
    margin-bottom: 16px;   
    display: flex; 
    flex-wrap: wrap;   
    gap: 8px 8px;   
}

.topic-image {
    width: 200px;
    display: block;
    margin-bottom: 10px;
}

.custom-tag {
    transition: box-shadow 0.3s ease;
}

.custom-tag:hover {
    box-shadow: 0 0 0 3px rgba(0, 0, 0, 0.2);
}
.see-more-btn {
    color: #3aaf00;
    background-color: #f5f5f5;
    border-color: #919191;
    transition: background-color 0.3s, border-color 0.3s;
}
.see-more-btn:hover{
    background-color: rgb(48, 193, 156);
    border-color: rgb(48, 193, 156);
    color: white;
}

.see-more-btn-active{
    background-color: rgb(48, 193, 156);
    border-color: rgb(48, 193, 156);
    color: white;
}

.see-less-btn {
    color: #a83238;
    background-color: #f5f5f5;
    border-color:#919191;
    transition: background-color 0.3s, border-color 0.3s;
}

.see-less-btn:hover {
    background-color: rgb(216, 135, 135);
    border-color: rgb(216, 135, 135);
    color: white;
}
.see-less-btn-active{
    background-color: rgb(216, 135, 135);
    border-color: rgb(216, 135, 135);
    color: white;
}


.explanation-container h3 {
    margin-top: 0;
    /* Removes the top margin from the heading if needed */
    margin-bottom: 8px;
    /* Adds some space above the explanation text */
}

.explanation-container p {
    margin-top: 10;
    /* Adjusts spacing as needed */
}

.rate-container {
    margin-bottom: 20px;
    /* Adds space below the rating component */
}

.not-recommend-text {
    margin-top: 20px;
    /* Adds space above the text */
    margin-bottom: 5px;
    /* Adds space below the text for separation from options */
    font-weight: bold;
    /* Makes the text bold */
    font-size: 17px;
}

.selected-option {
    background-color: rgb(179, 61, 68);
    border-color: rgb(179, 61, 68);
    color: white;
}

.selected-option:hover {
    color: #ffee8f;
}


.hover-option:hover {
    color: brown;
    background-color: #f5f5f5;
    border-color: #d9d9d9;
    transition: background-color 0.3s, border-color 0.3s;
}
</style>
