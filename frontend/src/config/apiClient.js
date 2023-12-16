// import axios from 'axios';
import globalState from './globalState';
import axios from 'axios';
import JSZip from 'jszip';

const apiClient = axios.create({
    baseURL: 'http://localhost:8081',
    headers: {
        'Content-Type': 'application/json'
    }
});

const API_KEY = 'AIzaSyCScOmixSUEzSbl7AiTmw-oCWzH16RJNk0'; // Replace with your YouTube Data API key

export default {
    login(userId) {
        return apiClient.post('/login', { userId });

        // TODO: Remove this mock request
        // globalState.userId = userId;
        // return {
        //     status: 200,
        //     message: 'Login successful'
        // }
    },
    async getVideos() {
       
        try {
            let response;
            if (globalState.userId) {
                response = await apiClient.get(`/videos/recommendations?userId=${globalState.userId}`);
            } else {
                response = await apiClient.get('/videos/all');
            }
            return response;
        } catch (error) {
            console.error('Error fetching videos:', error);
            throw error;
        }
    },

    async searchVideos(keyword) {
        return apiClient.get(`/youtube/search/${keyword}`);
        // try {
        //     const searchUrl = `https://www.googleapis.com/youtube/v3/search?key=${API_KEY}&q=${encodeURIComponent(keyword)}&part=snippet&maxResults=${maxResults}`;
        //     const searchResponse = await axios.get(searchUrl);

        //     const videoIds = searchResponse.data.items.map(item => item.id.videoId).join(',');
        //     const details = await this.getDetails(videoIds, regionCode);
        //     const videos = details.map(detail => ({
        //         url: `https://www.youtube.com/embed/${detail.id}`,
        //         ...detail
        //     }));

        //     // for test: download video details separately
        //     await this.createAndDownloadZip(videos, keyword + '.zip');

        //     return {
        //         status: 200,
        //         data: videos
        //     };
        // } catch (error) {
        //     console.error('Error fetching videos:', error);
        //     return {
        //         status: error.response ? error.response.status : 500,
        //         message: error.message,
        //         data: []
        //     };
        // }
    },


    async getDetails(videoIds, regionCode = 'DE') {
        const detailsUrl = `https://www.googleapis.com/youtube/v3/videos?id=${videoIds}&key=${API_KEY}&part=snippet,topicDetails,statistics,status`;
        try {
            const response = await axios.get(detailsUrl);
            // this.downloadData(response.data, 'videoDetails.json'); // for test

            var videoDetails = response.data.items.filter(video =>
                video.status.embeddable &&
                video.status.privacyStatus !== 'private' &&
                (!video.status.regionRestriction ||
                    (!video.status.regionRestriction.blocked ||
                        !video.status.regionRestriction.blocked.includes(regionCode)))
            );

            

            return videoDetails;
        } catch (error) {
            console.error('Error in getDetails:', error);
            throw error;
        }
    },


    async fetchMostPopularVideos(maxResults = 50, regionCode = 'DE') {
        try {
            const popularVideosUrl = `https://www.googleapis.com/youtube/v3/videos?part=snippet,statistics,status&chart=mostPopular&maxResults=${maxResults}&regionCode=${regionCode}&key=${API_KEY}`;
            const response = await axios.get(popularVideosUrl);

            const videos = response.data.items
                .filter(video =>
                    video.status.embeddable &&
                    video.status.privacyStatus !== 'private' &&
                    (!video.status.regionRestriction ||
                        (!video.status.regionRestriction.blocked ||
                            !video.status.regionRestriction.blocked.includes(regionCode)))
                )
                .map(video => ({
                    url: `https://www.youtube.com/embed/${video.id}`,
                    ...video
                }));

            // for test: download video details separately
            await this.createAndDownloadZip(videos, 'mostPopularVideos.zip');
            return {
                status: 200,
                data: videos
            };
        } catch (error) {
            console.error('Error fetching most popular videos:', error);
            return {
                status: error.response ? error.response.status : 500,
                message: error.message,
                data: []
            };
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