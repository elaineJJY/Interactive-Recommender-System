export default {
    install(app) {
        const globalState = {
            userId: null,
            feedbacks: new Array(),
            interactions: new Array()
        };
        app.config.globalProperties.$globalState = globalState;
    }
};
