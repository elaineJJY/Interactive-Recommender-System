<template>
    <div v-for="(recommendation, index) in recommendations" :key="index" class="video-row" :ref="setVideoRef(index)">
        <VideoComponent 
            :videoInfo="recommendation.video" 
            :explanation="recommendation.explanation"
            @videoEnded="() => handleVideoEnded(index)" />
        <div v-if="index < recommendations.length - 1" class="divider"></div>
    </div>
</template>

<script setup>
import { defineProps, ref, onMounted, onUnmounted } from 'vue';
import VideoComponent from './VideoComponent.vue';

defineProps({
    recommendations: Array,
});

const videoElements = ref([]);
let currentIndex = ref(0);
let isScrolling = ref(false);

const handleVideoEnded = (index) => {
    scrollToNextVideo(index);
};

const scrollToNextVideo = (index) => {
    if (isScrolling.value || index >= videoElements.value.length - 1) return;
    isScrolling.value = true;
    currentIndex.value = index + 1;
    videoElements.value[currentIndex.value].scrollIntoView({ behavior: 'smooth', block: 'center' });
    setTimeout(() => (isScrolling.value = false), 1000);
};

const setVideoRef = (index) => (el) => {
    videoElements.value[index] = el;
};

const handleWheelOrKeyDown = (event) => {
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
};

const scrollToPrevVideo = (index) => {
    if (isScrolling.value || index <= 0) return; // Check if it is already scrolling or if it's the first video
    isScrolling.value = true;
    currentIndex.value = index - 1; // Update the current index to the previous video's index
    videoElements.value[currentIndex.value].scrollIntoView({ behavior: 'smooth', block: 'center' });
    setTimeout(() => (isScrolling.value = false), 1000); // Reset scrolling flag after 1 second
};


onMounted(() => {

    // window.addEventListener('wheel', handleWheelOrKeyDown);
    window.addEventListener('keydown', handleWheelOrKeyDown);
});

onUnmounted(() => {
    // window.removeEventListener('wheel', handleWheelOrKeyDown);
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
