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

    <!-- Registration Success Modal -->
    <a-modal v-model:open="registerSuccessModalVisible" title="Please select the topics you are interested in"
        style="width: 800px;">

        <template #footer>
            <a-button type="primary" @click="handleTopicSubmit">
                OK
            </a-button>
        </template>

        <div class="tags-container" v-if="topics">

            <a-popover v-for="(topic) in topics" :key="topic.topicNumber" placement="top" trigger="hover">
                <template #content>
                    <div>
                        <p>
                            <b>{{ topic.description }}</b>
                        </p>
                        <img class="topic-image"
                            :src="require(`@/assets/topics/topic_${topic.topicNumber}_wordcloud.png`)"
                            alt="Topic image">
                    </div>
                </template>
                <a-tag :color="topicColor.get(topic.topicNumber)?.color || 'gray'" class="custom-tag"
                    @click="registerForTopic(topic.topicNumber)">
                    {{ topic.description }}
                </a-tag>
            </a-popover>
        </div>

    </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted, defineEmits} from 'vue';
import apiClient from '@/config/apiClient';
import { ElMessage, ElButton, ElAvatar } from 'element-plus';
import { UserFilled } from '@element-plus/icons-vue';
import globalState from '@/config/globalState';

const emit = defineEmits(['refresh']);
const isLoggedIn = ref(false);
const formState = reactive({
    userId: null
});
const modalVisible = ref(false);
const registerSuccessModalVisible = ref(false);
const topics = ref([]);
const selectedTopic = ref([]);

const predefinedColors = ['magenta', 'red', 'volcano', 'orange', 'gold', 'lime', 'green', 'cyan', 'blue', 'geekblue', 'purple'];
const topicColor = reactive(new Map());


const toggleTopicSelection = (topicNumber) => {
    const current = topicColor.get(topicNumber) || { selected: false, color: 'gray' };
    const selected = !current.selected;
    const color = selected ? predefinedColors[topicNumber % predefinedColors.length] : 'gray';
    topicColor.set(topicNumber, { selected, color });
};


const registerForTopic = async (topicNumber) => {
    if (selectedTopic.value.includes(topicNumber)) {
        selectedTopic.value = selectedTopic.value.filter((topic) => topic !== topicNumber);
    } else {
        selectedTopic.value.push(topicNumber);
    }
    toggleTopicSelection(topicNumber);
    
};

const handleTopicSubmit = async () => {
    if (selectedTopic.value.length === 0) {
        openNotification('error', 'Please select at least one topic');
        return;
    }
    
    const response = await apiClient.registerTopics(selectedTopic.value);

    if (response.status === 200) {
        registerSuccessModalVisible.value = false;
        openNotification('success', 'Topics registered successfully!');
        emit('refresh');
    }
    
};

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
            topics.value = await apiClient.getTopics();
            registerSuccessModalVisible.value = true;
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
            emit('refresh');
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

.tags-container {
    margin: 5px;
    margin-bottom: 16px;
    display: flex;
    flex-wrap: wrap;
    gap: 8px 8px;
    scrollbar-width: thin;
    -ms-overflow-style: thin;
    max-height: 300px;
    overflow-y: auto;
}

.topic-image {
    width: 200px;
    display: block;
    margin-bottom: 10px;
}

.custom-tag {
    transition: box-shadow 0.3s ease;
}
</style>

