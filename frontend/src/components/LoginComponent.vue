<template>
    <!-- Show if not logged in -->
    <div v-if="!isLoggedIn" class="login-container">
        <el-input 
        placeholder="Enter User ID" 
        v-model="userId" 
        :suffix-icon="User" 
        class="login-input"
        size="large"
        ></el-input>
        <el-button @click="login" size="large" color="#2c71b4">Login</el-button>
    </div>

    <!-- Show if logged in -->
    <div v-else class="user-info-container">
        <el-avatar :icon="UserFilled">

        </el-avatar>
        <b>{{ globalState.userId }}</b>
        <el-button @click="logout" size="large" color="#2c71b4">Logout</el-button>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import apiClient from '@/config/apiClient';
import { ElMessage, ElInput, ElButton, ElAvatar } from 'element-plus';
import { User, UserFilled } from '@element-plus/icons-vue';

const userId = ref('');
const isLoggedIn = ref(false);
const globalState = reactive({
    userId: null
});

onMounted(() => {
    globalState.userId = JSON.parse(localStorage.getItem('userId'));
    isLoggedIn.value = globalState.userId !== null;
});

const login = async () => {
    try {
        const response = await apiClient.login(userId.value);

        if (response.status === 200) {
            localStorage.setItem('userId', JSON.stringify(userId.value));
            globalState.userId = userId.value;
            isLoggedIn.value = true;
            openNotification('success', 'Logged in successfully!');
        } else {
            openNotification('error', 'Error logging in. Please try again.');
        }
    } catch (error) {
        console.error('Error logging in:', error);
        openNotification('error', 'Error logging in. Please try again.');
    }
};

const logout = async () => {
    globalState.userId = null;
    localStorage.removeItem('userId');
    openNotification('success', 'Logged out successfully!');
    isLoggedIn.value = false;
};

const openNotification = (type, message) => {
    ElMessage({
        message: message,
        type: type,
    });
};
</script>

<style scoped>
.login-container,
.user-info-container {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    margin-right: 0px;
}

.login-container>*,
.user-info-container>* {
    margin: 5px;
}

.login-input {
    width: 150px;
}
</style>

