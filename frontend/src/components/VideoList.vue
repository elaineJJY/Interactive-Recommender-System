<template>
    <div class="tiktok-scroll-container">
        <div v-for="(video, index) in videos" :key="index" class="video-row" :ref="setVideoRef">
            <VideoComponent :videoInfo="video" @videoEnded="handleVideoEnded" />
            <div v-if="index < videos.length - 1" class="divider"></div>
        </div>
    </div>
    

</template>


<script setup>
import { defineProps, ref,  onMounted, onUnmounted } from 'vue';

import VideoComponent from './VideoComponent.vue';
const props = defineProps({
    videos: {
        type: Array,
        required: true,
    },
});
const currentIndex = ref(0);
function handleVideoEnded() {
    console.log("handle");
    if (currentIndex.value < props.videos.length - 1) {
        currentIndex.value++;
        console.log("video:" + currentIndex.value);
    } else {
        // eslint-disable-next-line no-undef
        emit('videoListEnded');
    }
}

const videoElements = ref([]);

onMounted(() => {
    const observer = new IntersectionObserver((entries) => {
        entries.forEach((entry, index) => {
            if (entry.isIntersecting && entry.intersectionRatio >= 0.5) {
                const nextIndex = index + 1;
                if (nextIndex < videoElements.value.length) {
                    videoElements.value[nextIndex].scrollIntoView({ behavior: 'smooth' });
                }
            }
        });
    }, { threshold: 0.5 });

    videoElements.value.forEach((el) => {
        if (el) {
            observer.observe(el);
        }
    });

    onUnmounted(() => {
        observer.disconnect();
    });
});

const setVideoRef = (el) => {
    if (el) {
        videoElements.value.push(el);
    }
};
</script>


<style>
.tiktok-scroll-container {
    overflow-y: scroll;
    height: 100%;
    scroll-snap-type: y mandatory;
}

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
