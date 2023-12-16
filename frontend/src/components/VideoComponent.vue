<template>
    <el-card :body-style="{ padding: '0px' }">
        <div class="video-wrapper">
            <iframe :src="buildURL(videoInfo.id)" :title="videoInfo.snippet.title" frameborder="0"
                allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
                allowfullscreen></iframe>
        </div>
        <div style="padding: 14px">
            <span>{{ videoInfo.snippet.title }}</span>
            <div class="bottom">
                <!-- <span>{{ videoInfo.snippet.description }}</span> -->
                <el-button text class="button">
                    <el-icon class="info-icon">
                        <InfoFilled />
                    </el-icon>
                    <span class="info-text">Info</span>
                </el-button>
            </div>
        </div>
    </el-card>
</template>

<script setup>
import { defineProps } from 'vue';
import { InfoFilled } from '@element-plus/icons-vue';
defineProps({
    videoInfo: {
        type: Object,
        required: true,
    },
});

function buildURL(videoId) {
    return `https://www.youtube.com/embed/${videoId}`;
}
</script>

<style scoped>
/* Ensure the video wrapper maintains a 16:9 aspect ratio */
.video-wrapper {
    position: relative;
    width: 100%;
    padding-bottom: 56.25%;
    /* 16:9 aspect ratio (9 / 16 * 100%) */
}

iframe {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
}

/* Style for the button and other elements */
.bottom {
    margin-top: 13px;
    line-height: 12px;
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.button {
    padding: 5px;
    min-height: auto;
}

/* Style for the Info icon and text */
.info-icon {
    margin-right: 5px;
    /* Add spacing between the icon and text */
    color: #2c70b4c5;
}

.info-text {
    color: #2c70b4c5;
    /* Change the text color to light blue */
}
</style>
