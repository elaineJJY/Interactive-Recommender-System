<template>
    <div class="video-component">
        <iframe id="video-player" ref="videoFrame" :src="buildURL(videoInfo.id)" :title="videoInfo.snippet.title"
            frameborder="0"
            allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture; web-share"
            allowfullscreen class="video-frame"></iframe>
        <!-- <div class="video-info-card">

            <div class="bottom">
                <div>
                    <span class="info-icon">
                        <InfoFilled />
                    </span>
                    <span class="info-text">{{ videoInfo.snippet.title }}</span>
                </div>
                <div>
                    <span class="info-icon">
                        <LikeFilled />
                    </span>
                    <span class="info-text">{{ videoInfo.statistics.likeCount }}</span>
                </div>
            </div>
            
        </div> -->
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
                        <DislikeTwoTone :two-tone-color="dislikeClicked ? '#eb2f96' : '#000000'" />
                    </template>
                </a-button>
            </a-space-compact>
        </div>
    </div>
</template>

<script setup>
/* global YT */
import { defineProps, onMounted, onUnmounted, ref } from 'vue';
import { InfoCircleFilled, LikeTwoTone, DislikeTwoTone } from '@ant-design/icons-vue';
import apiClient from '@/config/apiClient';
const props = defineProps({
    videoInfo: {
        type: Object,
        required: true,
    },
});

const likeClicked = ref(false);
const dislikeClicked = ref(false);


const videoFrame = ref(null);

function buildURL(videoId) {
    return `https://www.youtube.com/embed/${videoId}?enablejsapi=1&origin=${window.location.origin}`;
}

let observer;
onMounted(() => {
    if (videoFrame.value) {
        observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    videoFrame.value.contentWindow.postMessage('{"event":"command","func":"playVideo","args":""}', '*');
                } else {
                    videoFrame.value.contentWindow.postMessage('{"event":"command","func":"pauseVideo","args":""}', '*');
                }
            });
        }, {
            threshold: 0.5
        });

        observer.observe(videoFrame.value);
    }

});
onUnmounted(() => {
    if (observer) {
        observer.disconnect();
    }
});

// eslint-disable-next-line no-unused-vars
let player;
onMounted(() => {
    if (!window.YT) {
        const tag = document.createElement('script');
        tag.src = "https://www.youtube.com/iframe_api";
        const firstScriptTag = document.getElementsByTagName('script')[0];
        firstScriptTag.parentNode.insertBefore(tag, firstScriptTag);
    }

    window.onYouTubeIframeAPIReady = () => {
        player = new YT.Player('video-player', {
            events: {
                'onStateChange': onPlayerStateChange
            }
        });
    };
});

function onPlayerStateChange(event) {
    if (event.data === YT.PlayerState.ENDED) {
        // eslint-disable-next-line no-undef
        // emit('videoEnded');
    }
}

function submitLikeFeedback() {
    likeClicked.value = !likeClicked.value;
    if (dislikeClicked.value) dislikeClicked.value = false;
    const feedback = {
        videoId: props.videoInfo.id,
        rating: 1
    };
    apiClient.submitFeedback(feedback);
}

function submitDislikeFeedback() {
    dislikeClicked.value = !dislikeClicked.value;
    // Reset like button if it was clicked
    if (likeClicked.value) likeClicked.value = false;
    const feedback = {
        videoId: props.videoInfo.id,
        rating: -1
    };
    apiClient.submitFeedback(feedback);
}

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

.video-frame {
    width: 45vh;
    height: 100%;
    border-radius: 10px;
    box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.1);
    border-radius: 10px;
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