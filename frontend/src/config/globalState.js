export default {
    install(app) {
        const globalState = {
            userId: null,
            videos: [],
        };
        app.config.globalProperties.$globalState = globalState;
    }
};
