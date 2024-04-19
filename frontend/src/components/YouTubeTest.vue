<template>
    <div>
        <YoutubeVue3 ref="youtubePlayer" videoid="18ngY1cCAiA" @ended="onEnded" @paused="onPaused" @played="onPlayed"
            autoplay="0" controls="1" :height="'95%'" scrolling="no" :muted="true"
            style="margin-top:10px; border-radius: 10px; box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1); overflow-y: hidden;" />
    </div>
</template>

<script>
import { YoutubeVue3 } from 'youtube-vue3'

export default {
    components: {
        YoutubeVue3,
    },
    data() {
        return {
            videoInfo: {
                id: 'your-video-id',
            },
        };
    },
    methods: {
        onEnded() {
            // Handle video ended event
        },
        onPaused() {
            // Handle video paused event
        },
        onPlayed() {
            // Handle video played event
        },
    }
};
</script>

<style>
/* Add your custom styles here */
</style>