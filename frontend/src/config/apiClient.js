// import axios from 'axios';
import globalState from './globalState';
import axios from 'axios';
import JSZip from 'jszip';
import { ElMessage } from 'element-plus';

const apiClient = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json'
    }
});

export default {
    register(userId) {
        return apiClient.post('/users/register', { userId: userId });
    },
    login(userId) {
        return apiClient.post('/users/login', { userId: userId });
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
            if(!globalState.feedbacks) {
                globalState.feedbacks = new Array();
            }
            globalState.feedbacks.push(feedback);
            if (globalState.feedbacks.length >= 5) {
                await apiClient.post(`/feedback`, globalState.feedbacks);
                globalState.feedbacks.length = 0;
            }
        
            return true;
        }
        catch (error) {
            console.error('Error saving feedback:', error);
            throw error;
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