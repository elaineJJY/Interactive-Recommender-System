export default {
    install(app) {
        const globalState = {
            userId: null,
            
        };
        app.config.globalProperties.$globalState = globalState;
    }
};
