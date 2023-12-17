<template>
    <div class="tiktok-scroll-container">
            <div v-for="(video, index) in videos" :key="index" class="video-row">
                <VideoComponent :videoInfo="video"  @videoEnded="handleVideoEnded"/>
                <div v-if="index < videos.length - 1" class="divider"></div>
            </div>
    </div>
</template>


<script setup>
import { defineProps,ref } from 'vue';

import VideoComponent from './VideoComponent.vue';
defineProps({
    videos: Array
});
const currentIndex = ref(0);
function handleVideoEnded() {
    console.log("handle");
    if (currentIndex.value < this.videos.length - 1) {
        currentIndex.value++;
        console.log("video:"+currentIndex.value);
    } else {
        // eslint-disable-next-line no-undef
        emit('videoListEnded');
    }
}
</script>


<style>
.tiktok-scroll-container {
    overflow-y: scroll;
    /* Enable vertical scrolling */
    height: 100%;
    /* Full viewport height */
    scroll-snap-type: y mandatory;
    /* Enable snap scrolling */
}

.video-row {
    height: 100%;
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
