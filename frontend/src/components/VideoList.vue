<template>
    <div>
        <el-row :gutter="5">
            <el-col :span="8" v-for="(video, index) in videos" :key="index">
                <VideoComponent :videoInfo="video" />
            </el-col>
        </el-row>
    </div>
</template>

<script setup>
import { defineProps } from 'vue';

import VideoComponent from './VideoComponent.vue';

// Define the 'videos' prop
defineProps({
    videos: Array
});


</script>
