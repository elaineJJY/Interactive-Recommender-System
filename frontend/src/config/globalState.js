export default {
    install(app) {
        const globalState = {
            userId: null,
            feedbacks: new Array(),
            interactions: new Array(),
            round: 0,
        };
        app.config.globalProperties.$globalState = globalState;
    }
};
