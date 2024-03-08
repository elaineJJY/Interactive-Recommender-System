<template>
    <div v-for="(recommendation, index) in recommendations" :key="index" class="video-row">
        <VideoComponent 
            :ref="setVideoRef(index)"
            :videoInfo="recommendation.video" 
            :explanation="recommendation.explanation"
            :topics="recommendation.topics"
            @videoEnded="() => handleVideoEnded(index)" 
            @updateIndex="handleUpdateIndex(index)"/>
        <div v-if="index < recommendations.length - 1" class="divider"></div>
    </div>
</template>

<script setup>
import { defineProps, ref, onMounted, onUnmounted, defineEmits, nextTick} from 'vue';
import VideoComponent from './VideoComponent.vue';
import { ElNotification } from 'element-plus';
import { h } from 'vue';
import { ElIcon } from 'element-plus';
import { Loading } from '@element-plus/icons-vue';

const props = defineProps({
    recommendations: Array,
});

const videoElements = ref([]);
let currentIndex = ref(0);
let isScrolling = ref(false);

const emit = defineEmits(['videoListEnded']);

const handleUpdateIndex = (index) => {
    currentIndex.value = index;
    if (index >= props.recommendations.length - 1) {
        // If it's the last video, get more recommendations
     
        ElNotification({
            title: 'Loading more recommendations...',
            message: h('i', { style: 'color: teal' }, 'We are recalculating the videos you might be interested in, based on your usage habits and modified preferences'),
            icon: h(ElIcon, { size: 20 }, () => [h(Loading)]),
            offset: 50,
            duration: 5000,
        })
        emit('videoListEnded');
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
});

onUnmounted(() => {
    window.removeEventListener('wheel', handleWheelOrKeyDown);
    window.removeEventListener('keydown', handleWheelOrKeyDown);
});
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
</style>
