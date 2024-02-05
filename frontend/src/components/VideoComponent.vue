<template>
    <div class="video-component" ref="videoContainer">
        
        <!-- Youtube video -->
        <YoutubeVue3 ref="youtubePlayer" 
          :videoid="videoInfo.id"
          @ended="onEnded"
          autoplay="0"
          controls=1
          :height="'100%'"
          style="border-radius: 10px; box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);"
        />

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
                            <a-form model="explanation">
                                <a-form-item
                                    v-for="(value, key) in videoInfo"
                                    :key="key"
                                    :label="key.charAt(0).toUpperCase() + key.slice(1)">
                                    <a-input :value="value" disabled></a-input>
                                </a-form-item>
                            </a-form>
                        </div>
                        <div>
                            <h4>Explanation</h4>
                            <p>{{explanation}}</p>
                        </div>
                    </template>
                    <a-button shape="circle" size="large">
                        <template #icon>
                        <InfoCircleFilled />
                        </template>
                    </a-button>
                </a-popover>
                
                <a-button shape="circle" size="large" @click="submitLikeFeedback">
                    <template #icon>
                        <LikeTwoTone :two-tone-color="likeClicked ? '#f5222d' : '#000000'" />
                    </template>
                </a-button>
                <a-button shape="circle" size="large" @click="submitDislikeFeedback">
                    <template #icon>
                        <DislikeTwoTone :two-tone-color="dislikeClicked ? '#52c41a' : '#000000'" />
                    </template>
                </a-button>
            </a-space-compact>
        </div>
    </div>
</template>

<script setup>
import { ref, defineProps, defineEmits, onMounted, onUnmounted, nextTick  } from 'vue';
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

// function calcWidth(height) {
//     return `calc(${height} * 16 / 9 )`;
//     // return `calc(${height} * 1 / 2 )`; 
// }

// feedback sent to the server when the video is ended
let feedback = {
    videoId: props.videoInfo.id,
    rating: -1, // -1 means no feedback
    totalWatchTime: 0,
};

function onEnded() {
    emit('videoEnded');
}

const submitLikeFeedback = async () => {
    if (!globalState.userId) {
        ElMessage.error('Please login first');
        return false;
    }
    feedback.rating = 5;
    
    likeClicked.value = !likeClicked.value;
    if (dislikeClicked.value) dislikeClicked.value = false;
    
};

const submitDislikeFeedback = async () => {
    if (!globalState.userId) {
        ElMessage.error('Please login first');
        return;
    }
    feedback.rating = 0;
    
    dislikeClicked.value = !dislikeClicked.value;
    if (likeClicked.value) likeClicked.value = false;
};



let watchStartTime = 0; 
let observer;

onMounted(() => {
    nextTick(() => {
        observer = new IntersectionObserver(
            ([entry]) => {
                if (entry.intersectionRatio > 0.5) { // when 50% of the video is visible
                    
                    youtubePlayer.value.player.playVideo(); 
                    youtubePlayer.value.player.unMute();
                    watchStartTime = Date.now();
                    
                    // scroll to the center of the video automatically
                    entry.target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'center'
                    });

                    emit('updateIndex'); 

                    
                } else {
                    youtubePlayer.value.player.pauseVideo(); 
                    feedback.totalWatchTime = (Date.now() - watchStartTime);
                }
            },
            { threshold: [0.5] } 
        );

        if (videoContainer.value) {
            observer.observe(videoContainer.value); 
        }
    });
});

onUnmounted(() => {
    observer.disconnect(); 
    // alert('observer disconnected'+ props.videoInfo.id);
    apiClient.submitFeedback(feedback);

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
}

.a-space {
    margin-bottom: 60px;
    font-size: 20px;
}
</style>
