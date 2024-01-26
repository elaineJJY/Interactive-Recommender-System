<template>
  <el-container style="height: 100vh;">
    <el-header height="45px">
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

        <!-- Search Bar -->
        <el-col :span="8" :offset="6">
          <SearchBar @search="handleSearch" />
        </el-col>

        <!-- Login Component -->
        <el-col :span="7">
          <LoginCommponent />
        </el-col>
      </el-row>
    </el-header>

    <el-container style="margin-top: 60px; height: calc(100vh - 60px);">

      <el-aside :width="state.collapsed ? '80px' : '150px'" class="sidebar">
        <a-menu v-model:openKeys="state.openKeys" v-model:selectedKeys="state.selectedKeys" mode="inline"
          :inline-collapsed="state.collapsed" :items="items"></a-menu>
      </el-aside>

      <el-main>
        <!-- Content for Videos -->
        <div v-if="state.selectedKeys[0] === '1'">
          <VideoList :recommendations="recommendations" @videoListEnded="getRecommendations"/>
        </div>

        <!-- Content for Hot Videos-->
        <div v-else-if="state.selectedKeys[0] === '2'">
          Hot Videos
        </div>

        <!-- Content for Profile -->
        <div v-else-if="state.selectedKeys[0] === '3'">
          Profile
        </div>

        <!-- Content for Settings -->
        <div v-else-if="state.selectedKeys[0] === '4'">
          Setting
        </div>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import SearchBar from '@/components/SearchBar.vue';
import LoginCommponent from '@/components/LoginComponent.vue';
import VideoList from './components/VideoList.vue';
import apiClient from '@/config/apiClient';
import { reactive, watch, h, ref, onMounted } from 'vue';
import globalState from '@/config/globalState';
import {
  PieChartOutlined,
  FireOutlined,
  SettingOutlined,
  VideoCameraOutlined,
} from '@ant-design/icons-vue';
import { Fold, Expand } from '@element-plus/icons-vue';

const state = reactive({
  collapsed: true,
  selectedKeys: ['1'],
  openKeys: ['sub1'],
  preOpenKeys: ['sub1'],
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
    icon: () => h(FireOutlined),
    label: 'Hot',
    title: 'Hot',
  },
  {
    key: '3',
    icon: () => h(PieChartOutlined),
    label: 'Profile',
    title: 'Profile',
  },
  {
    key: '4',
    icon: () => h(SettingOutlined),
    label: 'Settings',
    title: 'Settings',
  },
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


// Search Videos
const recommendations = ref([]);
onMounted(() => {
  globalState.userId = JSON.parse(localStorage.getItem('userId'));
  getRecommendations();

});

const getRecommendations = async () => {
  try {
    // add all videos to the videos array
    recommendations.value.push(...(await apiClient.getRecommendations()));
  } catch (error) {
    console.error('Error getting recommendations:', error);
  }
};
const handleSearch = async (query) => {
  try {
    recommendations.value = query == "" ? await apiClient.getRecommendations() : await apiClient.searchVideos(query);
  } catch (error) {
    console.error('Error getting videos:', error);
  }
};
</script>

<style>
.el-header {
  background-color: #fff;
  box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.12);
  z-index: 1000;
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
</style>
