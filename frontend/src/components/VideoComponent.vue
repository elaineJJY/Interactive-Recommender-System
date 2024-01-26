<template>
    <div class="video-component" ref="videoContainer">
        
        <!-- Youtube video -->
        <YoutubeVue3 ref="youtubePlayer" 
          :videoid="videoInfo.id"
          @ended="onEnded"
            autoplay="0"
          :width="calcWidth('100%')" 
          :height="'100%'"
          style="border-radius: 10px; box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);"
        />

        <!-- Side button -->
        <div class="video-button-list">
            <a-space-compact direction="vertical" size="large">
                <a-button shape="circle" size="large">
                    <template #icon>
                        <InfoCircleFilled />
                    </template>
                </a-button>
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
import { YoutubeVue3 } from 'youtube-vue3'
const props = defineProps({
    videoInfo: Object,
    explanation: String,
});

const emit = defineEmits(['videoEnded']);
const likeClicked = ref(false);
const dislikeClicked = ref(false);
const youtubePlayer = ref(null); 
const videoContainer = ref(null); 

function calcWidth(height) {
    return `calc(${height} * 1080 / 1920)`; 
}

function onEnded() {
    emit('videoEnded');
}

const submitLikeFeedback = async () => {
    const feedback = {
        videoId: props.videoInfo.id,
        rating: 1,
    };
    var isSubmitted = await apiClient.submitFeedback(feedback);

    if (isSubmitted) {
        likeClicked.value = !likeClicked.value;
        if (dislikeClicked.value) dislikeClicked.value = false;
    }
};

const submitDislikeFeedback = async () => {
    const feedback = {
        videoId: props.videoInfo.id,
        rating: -1,
    };
    var isSubmitted = await apiClient.submitFeedback(feedback);

    if (isSubmitted) {
        dislikeClicked.value = !dislikeClicked.value;
        if (likeClicked.value) likeClicked.value = false;
    }
};
let observer;

onMounted(() => {
    nextTick(() => {
        observer = new IntersectionObserver(
            ([entry]) => {
                if (entry.intersectionRatio > 0.5) { // when 50% of the video is visible
                    
                    youtubePlayer.value.player.playVideo(); 
                    
                    // scroll to the center of the video automatically
                    entry.target.scrollIntoView({
                        behavior: 'smooth',
                        block: 'center'
                    });
                    
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
