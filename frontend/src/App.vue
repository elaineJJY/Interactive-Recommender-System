<template>
  <el-container style="height: 100vh;">
    <el-header height="50px">
      <el-row align="middle" justify-content="end">
        <el-col :span="3">
          <!-- Toggle Button -->
          <el-button @click="toggleCollapsed" text size="large">
            <el-icon v-if="state.collapsed" size="large">
              <Expand />
            </el-icon>
            <el-icon v-else size="large">
              <Fold />
            </el-icon>
          </el-button>

          <!-- TUM Logo -->
          <!-- <img :src="require('@/assets/tum_logo.png')" alt="TUM Logo"
            style="height: 30px; width: auto; margin-left: 10px;"> -->
        </el-col>
        <el-col :span="6">
          <h1 style="font-size: 18px; font-weight: bold; margin-top: 5px; ">
            <span v-if="state.selectedKeys[0] === '1'">Videos</span>
            <span v-else-if="state.selectedKeys[0] === '2'">User Profile</span>
          </h1>
        </el-col>

        <!-- Search Bar -->
        <el-col :span="8">
          <!-- <SearchBar v-if="state.selectedKeys[0] === '1'" @search="handleSearch" /> -->
        </el-col>

        <!-- Login Component -->
        <el-col :span="7">
          <LoginCommponent @refresh="handleRefresh" />
        </el-col>
      </el-row>
    </el-header>

    <el-container style="margin-top: 50px; height: calc(100vh - 60px);">

      <el-aside :width="state.collapsed ? '80px' : '150px'" class="sidebar">
        <a-menu v-model:openKeys="state.openKeys" v-model:selectedKeys="state.selectedKeys" mode="inline"
          :inline-collapsed="state.collapsed" :items="items"></a-menu>
      </el-aside>

      <el-main>
        <!-- Content for Videos -->
        <div v-if="state.selectedKeys[0] === '1'">
          <div v-if="showEmpty"
            style="display: flex; justify-content: center; align-items: center; height: 93vh; border: 100px solid #f0f2f5;">
            <a-empty description="Please log in first to start the survey" />
          </div>
          <VideoList v-else ref="videoListRef" />

        </div>

        <!-- Content for Hot Videos-->
        <div v-else-if="state.selectedKeys[0] === '2'">
          <UserProfile ref="userProfileRef" class="user-profile-button" />
        </div>

      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import LoginCommponent from '@/components/LoginComponent.vue';
import VideoList from './components/VideoList.vue';
import UserProfile from './components/UserProfile.vue';
import apiClient from '@/config/apiClient';
import { reactive, watch, h, onMounted, onUnmounted, ref, provide } from 'vue';
import globalState from '@/config/globalState';

// import YouTubeTest from './components/YouTubeTest.vue'; // For Test
import {
  SettingOutlined,
  VideoCameraOutlined,
} from '@ant-design/icons-vue';
import { Fold, Expand } from '@element-plus/icons-vue';

const showEmpty = ref(false);
const state = reactive({
  collapsed: false,
  selectedKeys: ['1'],
  openKeys: ['sub1'],
  preOpenKeys: ['sub1']
});

const items = reactive([
  {
    key: '1',
    icon: () => h(VideoCameraOutlined),
    label: 'Videos',
    title: 'Videos',
  },
  {
    key: '2',
    icon: () => h(SettingOutlined),
    label: 'Profile',
    title: 'Profile',
  }
]);
watch(
  () => state.openKeys,
  (_val, oldVal) => {
    state.preOpenKeys = oldVal;
  },
);

const toggleCollapsed = () => {
  state.collapsed = !state.collapsed;
  state.openKeys = state.collapsed ? [] : state.preOpenKeys;
};

const videoListRef = ref(null);
function handleRefresh() {
  if (videoListRef.value && state.selectedKeys[0] === '1') {
    videoListRef.value.refreshList();
  }
  if (userProfileRef.value && state.selectedKeys[0] === '2') {
    userProfileRef.value.refreshUserProfile();
  }
  showEmpty.value = globalState.userId === null;
}

const userProfileRef = ref(null);


onMounted(() => {
  globalState.userId = JSON.parse(localStorage.getItem('userId'));
  showEmpty.value = globalState.userId === null;
  if (!globalState.round) {
    globalState.round = 1;
  }
  if(!globalState.interactions) {
    globalState.interactions = new Array();
  }
  provide('userProfileRef ', userProfileRef);
});


onUnmounted(() => {
  apiClient.onWebClose();
});

</script>

<style>
::-webkit-scrollbar {
    display: none;
}


.el-header {
  background-color: #fff;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.12);
  z-index: 300;
  position: fixed;
  width: 100%;
  margin-top: 2px;
}

.el-aside {
  height: 100%;
  overflow-y: auto;
}

.el-main {
  height: 100%;
  overflow-y: auto;
}

.sidebar {
  z-index: 300;
}

</style>
