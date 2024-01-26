export default {
    install(app) {
        const globalState = {
            userId: null,
            feedbacks: new Array()
        };
        app.config.globalProperties.$globalState = globalState;
    }
};
