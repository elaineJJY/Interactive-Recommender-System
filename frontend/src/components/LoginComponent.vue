<template>
    <!-- Show if not logged in -->
    <div v-if="!isLoggedIn" class="login-container">

        <!-- Sign In button, clicking this will show the modal -->
        <el-button @click="showModal" color="#2c71b4">Sign In</el-button>

        <!-- Login/Register modal -->
        <a-modal v-model:visible="modalVisible" title="Sign In" footer="" style="width: 300px;">
          <a-form model="formState">
            <a-form-item label="Username">
              <a-input v-model:value="formState.userId" placeholder="Enter your username"></a-input>
            </a-form-item>
          </a-form>
          <div style="display: flex; justify-content: space-between;">
            <a-tooltip title="Create a new user">
                  <a-button @click="register">Register</a-button>
                </a-tooltip>
            <a-tooltip title="Click to log in">
              <a-button type="primary" @click="login">Login</a-button>
            </a-tooltip>
            
          </div>
        </a-modal>
    </div>

    <!-- Show if logged in -->
    <div v-else class="user-info-container">
        <el-avatar :icon="UserFilled">

        </el-avatar>
        <b>{{ formState.userId }}</b>
        <el-button @click="logout" color="#2c71b4">Logout</el-button>
    </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import apiClient from '@/config/apiClient';
import { ElMessage, ElButton, ElAvatar } from 'element-plus';
import { UserFilled } from '@element-plus/icons-vue';
import globalState from '@/config/globalState';

const isLoggedIn = ref(false);
const formState = reactive({
    userId: null
});
const modalVisible = ref(false);

onMounted(() => {
    formState.userId = JSON.parse(localStorage.getItem('userId'));
    isLoggedIn.value = formState.userId !== null;
});

const showModal = () => {
    modalVisible.value = true;
};

const register = async () => {
    try {
       
        const response = await apiClient.register(formState.userId);

        if (response.status === 200) {
            localStorage.setItem('userId', JSON.stringify(formState.userId));
            isLoggedIn.value = formState.userId !== null;
            globalState.userId = isLoggedIn.value ? formState.userId : null;
            openNotification('success', 'Registered successfully!');
        } else {
            openNotification('error', response.data.message);
        }
    } catch (error) {
        console.error('Error registering:', error);
        openNotification('error', error.response.data);
    }
};

const login = async () => {
    try {
        
        const response = await apiClient.login(formState.userId);

        if (response.status === 200) {
            localStorage.setItem('userId', JSON.stringify(formState.userId));
            isLoggedIn.value = formState.userId !== null;
            globalState.userId = isLoggedIn.value ? formState.userId : null;
            openNotification('success', 'Logged in successfully!');
        } else {
            openNotification('error', response.data.message);
        }
    } catch (error) {
        console.error('Error logging in:', error);
        openNotification('error', error.response.data);
    }
};

const logout = async () => {
    formState.userId = null;
    localStorage.removeItem('userId');
    openNotification('success', 'Logged out successfully!');
    isLoggedIn.value = false;
    globalState.userId = null;
};

const openNotification = (type, message) => {
    ElMessage({
        message: message,
        type: type,
    });
};
</script>

<style scoped>
.login-container{
    display: flex;
    align-items: center;
    justify-content: flex-end;
    margin-right: 0px;
    
}

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

