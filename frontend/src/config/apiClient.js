// import axios from 'axios';
import globalState from './globalState';
import axios from 'axios';
import JSZip from 'jszip';

const apiClient = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json'
    }
});

export default {
    login(userId) {
        return apiClient.post('/login', { userId: userId });
    },
    async getRecommendations() {

        try {
            let response = await apiClient.get(`/videos/recommendations?userId=${globalState.userId}`);

            return response.data;
        } catch (error) {
            console.error('Error fetching videos:', error);
            throw error;
        }
    },

    async searchVideos(keyword, page = 0) {
        try {
            console.log('Searching videos for keyword:', keyword);
            let response = await apiClient.get(`/videos/search?keyword=${keyword}&page=${page}`);
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
                throw new Error('User is not logged in');
            }
            feedback.timestamp = new Date().toISOString();
            let response = await apiClient.post(`/feedback`, feedback);
            return response.data;
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