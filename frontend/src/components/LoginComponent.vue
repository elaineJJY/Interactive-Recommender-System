<template>
    <!-- Show if not logged in -->
    <div v-if="!isLoggedIn" class="login-container">

        <!-- Sign In button, clicking this will show the modal -->
        <el-button @click="showModal" color="#2c71b4">Sign In</el-button>

        <!-- Login/Register modal -->
        <a-modal v-model:visible="loginModalVisible" title="Sign In" footer="" style="width: 300px;">
            <a-form model="formState">
                <a-form-item label="Username">
                    <a-input v-model:value="formState.userId" placeholder="Enter your username"></a-input>
                </a-form-item>
            </a-form>
            <div style="display: flex; justify-content: space-between;">
                <a-tooltip title="Create a new user">
                    <a-button @click="questionnaireModalVisible = true; loginModalVisible=false">Register</a-button>
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

    <!-- Questionnaire Modal -->
    <a-modal v-model:open="questionnaireModalVisible"
        title="Welcome to the platform! Please fill out all the questions to complete your registration."
        :closable=false style="top: 20px; height: 800px; width:1200px; display: flex; flex-direction: column;">

        <div class="regiester-form">
            <a-form model="formState" style="margin-bottom: -15px; margin-top: 10px;">
                <a-form-item label="Username" style="width: 300px;">
                    <a-input v-model:value="formState.userId" placeholder="Enter your username"></a-input>
                </a-form-item>
            </a-form>
        </div>
        <PerfectScrollbar class="tags-container">
            <div class="questionnaire-content">
                <h4>Social Media Use and Familiarity</h4>
                <!-- Social Media Usage Frequency -->
                <div>
                    <b style="margin-right: 20px;">How often do you use social media platforms?</b>
                    <a-radio-group v-model:value="answers.socialMediaFrequency" required>
                        <a-radio value="never">Never</a-radio>
                        <a-radio value="rarely">Rarely</a-radio>
                        <a-radio value="weekly">Weekly</a-radio>
                        <a-radio value="daily">Daily</a-radio>
                        <a-radio value="multiple_times">Multiple times a day</a-radio>
                    </a-radio-group>
                </div>

                <!-- Familiarity with Recommender Systems -->
                <div>
                    <b>How familiar are you with the features of social media platforms that use recommender systems
                        (e.g.,
                        content feeds, personalized suggestions)?</b>
                    <a-radio-group v-model:value="answers.recommenderSystemFamiliarity">
                        <a-radio value="not_familiar">Not familiar</a-radio>
                        <a-radio value="slightly_familiar">Slightly familiar</a-radio>
                        <a-radio value="familiar">Familiar</a-radio>
                        <a-radio value="very_familiar">Very familiar</a-radio>
                    </a-radio-group>
                </div>

                <!-- Uniform Options for Several Questions -->
                <div v-for="(question, index) in dsiQuestions" :key="index" class="inline-question">
                    <!-- <a-flex justify="space-between" align="flex-end"> -->
                    <b>{{ question }}</b>
                    <a-radio-group v-model:value="answers.dsiResponses[question]" size="small"
                        class="radio-group-inline" button-style="solid">
                        <a-radio-button value="strongly_disagree">Strongly Disagree</a-radio-button>
                        <a-radio-button value="disagree">Disagree</a-radio-button>
                        <a-radio-button value="neutral">Neutral</a-radio-button>
                        <a-radio-button value="agree">Agree</a-radio-button>
                        <a-radio-button value="strongly_agree">Strongly Agree</a-radio-button>
                    </a-radio-group>
                    <!-- </a-flex> -->
                </div>

                <!-- Technical Knowledge -->
                <h4>Technical Knowledge</h4>
                <div>
                    <b>After watching several short science fiction videos on a short-video platform, you noticed the
                        platform recommended more videos in the science fiction genre to you. What is this
                        recommendation
                        most likely based on?</b>
                    <a-radio-group v-model:value="answers.videoRecommendationBasis">
                        <a-radio value="listening_conversations">The platform listened to your conversations</a-radio>
                        <a-radio value="assume_preference">The platform assumes all users like science fiction
                            videos</a-radio>
                        <a-radio value="viewing_history">The platform analyzed your viewing history and recommended
                            similar
                            videos</a-radio>
                        <a-radio value="random">The platform randomly recommends videos</a-radio>
                        <a-radio value="genre_popularity">Science fiction videos are the current most-watched genre on
                            the
                            platform</a-radio>
                    </a-radio-group>
                </div>
                <div>
                    <b>If I want the recommender system to suggest more of a certain type of items, what method(s) can I
                        use to get it to recommend more? Please select all that you think are correct.</b>
                    <a-checkbox-group v-model:value="answers.methodsToInfluenceRS">
                        <a-checkbox value="like_favorite">Click “Like” or “Favorite” on items of that type</a-checkbox>
                        <a-checkbox value="delete_history">Regularly delete browsing history of other
                            content</a-checkbox>
                        <a-checkbox value="contact_service">Directly contact customer service to request changes to the
                            content recommendations</a-checkbox>
                        <a-checkbox value="use_search">Use the search function more to search for the type of items I am
                            interested in</a-checkbox>
                        <a-checkbox value="disable_cookies">Disable cookies on your browser</a-checkbox>
                    </a-checkbox-group>
                </div>
            </div>
        </PerfectScrollbar>


        <template #footer>
            <a-button type="primary" @click="register" style="margin-top: -20px;">
                Submit
            </a-button>
        </template>

    </a-modal>

    <!-- Registration Success Modal: Preference Elicitation-->
    <a-modal v-model:open="registerSuccessModalVisible" title="Please select the topics you are interested in"
        style="top: 20px; height: 800px; width:1200px; display: flex; flex-direction: column;" :closable=false
        :maskClosable="false">

        <template #footer>
            <a-button type="primary" @click="handleTopicSubmit">
                OK
            </a-button>
        </template>

        <div v-if="topics">
            <PerfectScrollbar class="tags-container">
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
            </PerfectScrollbar>
        </div>

    </a-modal>
</template>

<script setup>
import { ref, reactive, onMounted, defineEmits} from 'vue';
import apiClient from '@/config/apiClient';
import { ElMessage, ElButton, ElAvatar } from 'element-plus';
import { UserFilled } from '@element-plus/icons-vue';
import globalState from '@/config/globalState';
import { PerfectScrollbar } from 'vue3-perfect-scrollbar'
import 'vue3-perfect-scrollbar/style.css';

const emit = defineEmits(['refresh']);
const isLoggedIn = ref(false);
const formState = reactive({
    userId: null
});
const loginModalVisible = ref(false);
const registerSuccessModalVisible = ref(false);
const questionnaireModalVisible = ref(false);
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
        if (selectedTopic.value.length >= 10) {
            openNotification('error', 'You can only select up to 10 topics');
            return;
        }
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
    loginModalVisible.value = true;
};

const register = async () => {
    try {
        // Check if all questions are answered
        if (!answers.socialMediaFrequency || !answers.recommenderSystemFamiliarity || Object.keys(answers.dsiResponses).length !== dsiQuestions.length || !answers.videoRecommendationBasis || answers.methodsToInfluenceRS.length === 0) {
            openNotification('error', 'Please answer all questions');
            return;
        }
        const response = await apiClient.register(formState.userId, answers);

        if (response.status === 200) {
            questionnaireModalVisible.value = false;
            localStorage.setItem('userId', JSON.stringify(formState.userId));
            isLoggedIn.value = formState.userId !== null;
            globalState.userId = isLoggedIn.value ? formState.userId : null;
            topics.value = await apiClient.getTopics();
            openNotification('success', 'Registered successfully!');
            registerSuccessModalVisible.value = true;
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



// Questionnaire Answers
const answers = reactive({
    socialMediaFrequency: null,
    recommenderSystemFamiliarity: null,
    dsiResponses: {},
    videoRecommendationBasis: null,
    methodsToInfluenceRS: []
});

const dsiQuestions = [
    'Checked truthfulness of online content past 3 months',
    'Seen doubtful online content past 3 months',
    'Expressed opinions on civic or political issues on websites or social media past 3 months',
    'Read privacy policy statements before providing data past 3 months',
    'Limited access to profile or content on social networks or online storage past 3 months',
    'Refused the use of personal data for advertising past 3 months',
    'Is concerned that online activities are being used for tailored advertising',
    'Changed settings of software, app or device past 3 months'
];

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
    max-height: 80vh !important;
    overflow-y: scroll;
    position: relative;
    -ms-overflow-style: thin;
    scrollbar-width: thin;
}

.topic-image {
    width: 200px;
    display: block;
    margin-bottom: 10px;
}

.custom-tag {
    transition: box-shadow 0.3s ease;
}

.custom-tag:hover {
   cursor: pointer;
}

.ps .ps__thumb-y {
    opacity: 1 !important;
}

.ps {
    max-height: 300px;
}

.questionnaire-content {
    margin-right: 15px;
}
.questionnaire-content>div {
    margin-bottom: 10px;
}
.questionnaire-content>h4 {
    margin-top: 10px;
    margin-bottom: 5px;
    background-color: rgb(214, 229, 239);
    border-radius: 5px;
    text-align: center;
    font-weight: bold;
}

.inline-question {
    margin-bottom: 0px !important;
    display: flex;
    justify-content: space-between;
}

.radio-group-inline {
    text-align: right !important;
}

.regiester-form {
    display: flex;
    align-items: center;
    justify-content: start;
    margin-bottom: -10px;
}


</style>

