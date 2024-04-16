// import axios from 'axios';
import globalState from './globalState';
import axios from 'axios';
import JSZip from 'jszip';
import { ElMessage, ElNotification } from 'element-plus';

const apiClient = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json'
    }
});

export default {
    register(userId, answers) {
        return apiClient.post('/users/register', { userId: userId, answers: answers});
    },
    async getTopics(){
        try {
            let response = await apiClient.get('/topics');
            return response.data;
        } catch (error) {
            console.error('Error fetching topics:', error);
            throw error;
        }
    },
    async registerTopics(topics) {
        try {
            console.log('registerTopics', topics);
            let response = await apiClient.post(`/topics/${globalState.userId}`,topics);
            return response;
        } catch (error) {
            console.error('Error registering topics:', error);
            throw error;
        }
    },
    login(userId) {
        return apiClient.post('/users/login', { userId: userId });
    },
    async getUser() {
        globalState.userId = localStorage.getItem('userId');
        if(globalState.userId) {
            try {
                let response = await apiClient.get(`/users/${globalState.userId}`);
                return response.data;
            } catch (error) {
                console.error('Error fetching user:', error);
                throw error;
            }
        }
        else {
            ElMessage.error('Please login first');
        }
    },
    async updateUser(user) {
        if (!globalState.userId) {
            ElMessage.error('Please login first');
            return;
        }
        try {
            let response = await apiClient.post(`/users/${globalState.userId}`, user);
            ElNotification({
                title: 'Success',
                message: 'User preferences updated successfully',
                type: 'success',
                duration: 2000,
            });
            return response.data;
        } catch (error) {
            console.error('Error updating user:', error);
            throw error;
        }
    },
    async getRecommendations() {

        try {
            let response = await apiClient.get(`/videos/recommendations?userId=${globalState.userId}`);
            console.log('getRecommendations', response.data);
            return response.data;
        } catch (error) {
            console.error('Error fetching videos:', error);
            throw error;
        }
    },

    async searchVideos(keyword, page = 0) {
        try {
            let response = await apiClient.get(`/videos/search?keyword=${keyword}&page=${page}`);
            console.log(response.data);
            return response.data;
        }
        catch (error) {
            console.error('Error searching videos:', error);
            throw error;
        }
    },
    async submitFeedback(feedback) {
        try {
            feedback.userId = globalState.userId;
            if (!feedback.userId) {
                ElMessage.error('Please login first');
                return false;
            }
            feedback.timestamp = new Date().toISOString();
            feedback.round = globalState.round;
            if(!globalState.feedbacks) {
                globalState.feedbacks = new Array();
            }
            globalState.feedbacks.push(feedback);
            if (globalState.feedbacks.length >= 1) {
                apiClient.post(`/feedback`, globalState.feedbacks);
                console.log('submitFeedback', globalState.feedbacks);
                globalState.feedbacks = new Array();
            }
        
            return true;
        }
        catch (error) {
            console.error('Error saving feedback:', error);
            throw error;
        }
    },
    async submitInteraction(interaction) {
        
        try {
            interaction.userId = globalState.userId;
            if (!interaction.userId) {
                return false;
            }
            interaction.timestamp = new Date().toISOString();
            interaction.round = globalState.round;
            console.log('submitInteraction', interaction);
            
            globalState.interactions.push(interaction);
            if (globalState.interactions.length >= 5) {
                await apiClient.post(`/interactions`, globalState.interactions);
                globalState.interactions = new Array();
            }
            return true;
        }
        catch (error) {
            console.error('Error saving interaction:', error);
            throw error;
        }
    },
    async onWebClose() {    
        if (globalState.feedbacks && globalState.feedbacks.length > 0) {
            apiClient.post(`/feedback`, globalState.feedbacks);
        }
        if (globalState.interactions && globalState.interactions.length > 0) {
            apiClient.post(`/interactions`, globalState.interactions);
        }
    },
    async downloadData(data, filename) {
        // Create a Blob from the data
        const blob = new Blob([JSON.stringify(data, null, 2)], { type: 'application/json' });

        // Create an anchor element and set the download attribute
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = filename;

        // Append the anchor to the document body and trigger the download
        document.body.appendChild(a);
        a.click();

        // Clean up by removing the anchor element
        document.body.removeChild(a);
    },

    async createAndDownloadZip(videos, filename = 'videos.zip') {
        const zip = new JSZip();

        videos.forEach((video) => {
            const fileName = video.id + '.json';
            const fileContent = JSON.stringify(video, null, 2);
            zip.file(fileName, fileContent);
        });


        zip.generateAsync({ type: 'blob' }).then(function (content) {
            const a = document.createElement('a');
            a.href = URL.createObjectURL(content);
            a.download = filename;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
        });
    }


}