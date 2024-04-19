<template>
    <a-spin size="large" class="spin-overlay" v-if="recommendations.length === 0"></a-spin>
    <div v-for="(recommendation, index) in recommendations" :key="index" class="video-row">
        <VideoComponent :ref="setVideoRef(index)" :videoInfo="recommendation.video"
            :explanation="recommendation.explanation" :topics="recommendation.topics" :videoIndex="index"
            @videoEnded="() => handleVideoEnded(index)" @updateIndex="handleUpdateIndex(index)" />
        <div v-if="index < recommendations.length - 1" class="divider"></div>
    </div>
    <a-modal v-model:open="endModalVisible" title="Notice" :closable=false>
        <template #footer>
            <a-button type="primary" @click="handleModalSubmit">
                Begin Questionnaire
            </a-button>
        </template>

        <p>
            You've successfully reached the end of this session. Thank you for your participation.
            The next step involves answering a questionnaire to gather your feedback.
        </p>
        <p>
            Please remember to use your username <strong>{{ globalState.userId }}</strong> when submitting your
            responses.
        </p>
    </a-modal>

</template>

<script setup>
import { ref, onMounted, onUnmounted, defineEmits, nextTick, defineExpose } from 'vue';
import VideoComponent from './VideoComponent.vue';
import { ElNotification } from 'element-plus';
import { h } from 'vue';
import { ElIcon } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';
import apiClient from '@/config/apiClient';
import globalState from '@/config/globalState';


const videoElements = ref([]);
let currentIndex = ref(0);
let isScrolling = ref(false);

const emit = defineEmits(['videoListEnded']);

const endModalVisible = ref(false);

const recommendations = ref([]);

onMounted(async() => {
    globalState.userId = JSON.parse(localStorage.getItem('userId'));
    await getRecommendations();
    try {
        videoElements.value[0].scrollIntoView();
    } catch (error) {
        console.log(error);
    }
});

const handleModalSubmit = async() => {
    await apiClient.onWebClose();
    
    window.open('https://docs.google.com/forms/d/e/1FAIpQLSdjDmWhLuEAM1EuuOdTjZdlpOpmb1uRmKl38AJQC0AnhnUYNw/viewform?usp=sf_link', '_blank');

};

const refreshList = async () => {
    recommendations.value = [];
    console.log('Refreshing video list...');
    await getRecommendations();
    try {
        videoElements.value[0].scrollIntoView();
    } catch (error) {
        console.log(error);
    }
}
const getRecommendations = async () => {
    try {
        // add all videos to the videos array
        recommendations.value.push(...(await apiClient.getRecommendations()));
    } catch (error) {
        console.error('Error getting recommendations:', error);
    }
};

const handleUpdateIndex = (index) => {
    currentIndex.value = index;
    if (index >= recommendations.value.length - 1) {
        // If it's the last video, get more recommendations
     
        ElNotification({
            title: 'Loading more recommendations...',
            message: h('i', { style: 'color: teal' }, 'We are recalculating the videos you might be interested in, based on your usage habits and modified preferences'),
            icon: h(ElIcon, { size: 20 }, () => [h(Loading)]),
            offset: 50,
            duration: 5000,
        })
        getRecommendations();
        emit('videoListEnded');
        if (globalState.userId) {
            globalState.round++;
            console.log('Round:', globalState.round);

            // finish the test after 10 rounds and navigate to the questionnaire page
            if (globalState.round > 10) {
                endModalVisible.value = true;
            }
        }
    }
};

const handleVideoEnded = (index) => {
    scrollToNextVideo(index);
};


const setVideoRef = (index) => (el) => {
    videoElements.value[index] = el;
};

let throttleTimer = false;
const throttle = (callback, time) => {
    if (throttleTimer) return;
    throttleTimer = true;
    setTimeout(() => {
        callback();
        throttleTimer = false;
    }, time);
};
const handleWheelOrKeyDown = async (event) => {
    if (event.target.tagName === 'INPUT' || event.target.tagName === 'TEXTAREA') return;
    if (isScrolling.value) return;
    await nextTick();
    event.preventDefault();
    throttle(async () => {
        try {
            var direction = event.type === 'wheel' ? event.deltaY > 0 ? 'down' : 'up' : event.key === 'ArrowDown' ? 'down' : 'up';
            videoElements.value[currentIndex.value].recordInteraction(event.type + '-' + direction);
        } catch (error) {
            console.error(error);
        }
        if (event.type === 'wheel') {

            // For mouse wheel event, deltaY is positive when scrolling down and negative when scrolling up
            if (event.deltaY > 0) {
                scrollToNextVideo(currentIndex.value); // Scroll down to the next video
            } else {
                scrollToPrevVideo(currentIndex.value); // Scroll up to the previous video
            }
        } else if (event.type === 'keydown') {

            if (event.key === 'ArrowDown') {
                scrollToNextVideo(currentIndex.value); // Scroll down to the next video
            } else if (event.key === 'ArrowUp') {
                scrollToPrevVideo(currentIndex.value); // Scroll up to the previous video
            }
        }

    }, 500); 
    
    
};

const touchStart = ref({ x: 0, y: 0 });
const handleTouchStart = (event) => {
    touchStart.value = { x: event.touches[0].clientX, y: event.touches[0].clientY };
};

const handleTouchMove = async(event) => {
    if (isScrolling.value) return;
    await nextTick();
    const touchEndY = event.changedTouches[0].clientY;
    const verticalMovement = touchStart.value.y - touchEndY;

    if (Math.abs(verticalMovement) > 10) { // Check if the user has moved their finger more than 10 pixels
        nextTick().then(() => {
            event.preventDefault();
            const direction = verticalMovement > 0 ? 'down' : 'up';
          
            throttle(async () => {
                try {
                    videoElements.value[currentIndex.value].recordInteraction('touch-' + direction);
                } catch (error) {
                    console.error(error);
                }
                if (direction === 'down') {
                    scrollToNextVideo(currentIndex.value);
                } else {
                    scrollToPrevVideo(currentIndex.value); 
                }
            }, 500);
        });
    }
};

const scrollToNextVideo = async (index) => {
    if (isScrolling.value || index >= videoElements.value.length - 1) return;
    isScrolling.value = true;
    await nextTick();
    
    try {
        videoElements.value[currentIndex.value].submitFeedback();
    } catch (error) {
        console.error(error);
    }
    
    await nextTick();
    
    var success = await videoElements.value[index + 1].scrollIntoView();
    
    if (success) {
        currentIndex.value = index + 1;
    }
    isScrolling.value = false;
};


const scrollToPrevVideo = async (index) => {
    if (isScrolling.value || index <= 0) return; // Check if it is already scrolling or if it's the first video
    isScrolling.value = true;
    await nextTick();
    try {
        videoElements.value[currentIndex.value].submitFeedback();
    } catch (error) {
        console.error(error);
    }
    
    await nextTick();
    var success = await videoElements.value[index - 1].scrollIntoView();
    if (success) {
        currentIndex.value = index - 1;
    }
    isScrolling.value = false;
};


onMounted(() => {
    window.addEventListener('wheel', handleWheelOrKeyDown, { passive: false });
    window.addEventListener('keydown', handleWheelOrKeyDown, { passive: false });
    window.addEventListener('touchstart', handleTouchStart, { passive: false });
    window.addEventListener('touchmove', handleTouchMove, { passive: false });
});

onUnmounted(() => {
    window.removeEventListener('wheel', handleWheelOrKeyDown);
    window.removeEventListener('keydown', handleWheelOrKeyDown);
    window.removeEventListener('touchstart', handleTouchStart);
    window.removeEventListener('touchmove', handleTouchMove);
});

defineExpose({
    refreshList
})
</script>

<style>
.video-row {
    height: 80%;
    scroll-snap-align: start;
}

.divider {
    height: 1px;
    background-color: rgba(221, 221, 221, 0.614);
    width: 60%;
    margin: 0 auto;
    margin-top: 50px;
    margin-bottom: 50px;
}

.spin-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgba(255, 255, 255, 0.8);
}

body {
    -ms-overflow-style: none;  /* IE & Edge */
    scrollbar-width: none;  /* Firefox */
}
</style>
