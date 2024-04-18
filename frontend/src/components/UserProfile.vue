<template>
    <div class="page-container">
        <div v-if="showEmpty"
            style="display: flex; justify-content: center; align-items: center; height: 100vh; border: 100px solid #f0f2f5;">
            <a-empty description="Please log in first to see your profile" />
        </div>

        <a-spin size="large" class="spin-overlay" v-if="showSpin"></a-spin>
        <a-row align="top" justify="space-between" v-if="!showEmpty">
            <a-col :span="5">
                <div class="model-container">
                    <div v-if="!inEdit" class="button-container">
                        <el-button type="primary" @click="inEdit = true" :icon="Edit">Edit</el-button>
                    </div>
                    <div v-else class="button-container">
                        <el-button type="success" @click="updateUserPreferences">Save</el-button>
                        <el-button @click="resetUserPreferences">Reset</el-button>
                    </div>

                </div>
                <div class="model-container">
                    <h2>Models</h2>
                    <a-typography-text strong style="color: gray;">
                        <blockquote>
                            Here you can tailor what your video feed looks like. In each recommendation round, 10 videos
                            will be recommended. Out of our 2 models, you can choose how many videos each model
                            recommends.
                        </blockquote>
                    </a-typography-text>


                    <!-- Models -->
                    <!-- Model 1 -->
                    <div class="model-slider-container" @mouseenter="isHovering[0] = true"
                        @mouseleave="isHovering[0] = false" :class="{ 'hover-border-topic': isHovering[0] }">
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <div style="font-size: 16px; font-weight: bold;">Model 1: Personalised Topic-based</div>
                            <a-button @click="showDetails[0] = true; saveInteraction('Model1: Info button')"
                                shape="circle" :icon="h(InfoOutlined)" size="small">
                            </a-button>
                        </div>

                        <el-input-number class="input-spacing" v-model="n_recs_per_model.personalised" :min="0"
                            :max="10" :step="1" :disabled="!inEdit"
                            @change="saveInteraction('Model1: Input Number',n_recs_per_model.personalised)" />
                        <el-dialog v-model="showDetails[0]" title="Model Details" width="30%" center>
                            <p>Personalised model: it takes your personal preferences into account.
                            </p>
                            <p>
                                The recommendations will represent a balance between topics you like and topics you have
                                not come across yet. The goal is for you to enjoy what you already like (exploitation)
                                while allowing you to discover new content (exploration). You can decide how this
                                balance is made using the personalisation slider.
                            </p>
                        </el-dialog>
                    </div>

                    <!-- Model 2 -->
                    <div class="model-slider-container" @mouseenter="isHovering[1] = true"
                        @mouseleave="isHovering[1] = false" :class="{ 'hover-border-topic': isHovering[1] }">
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <div style="font-size: 16px; font-weight: bold;">Model 2: Non-personalised Top-popular</div>
                            <a-button @click="showDetails[1] = true; saveInteraction('Model2: Info button')"
                                shape="circle" :icon="h(InfoOutlined)" size="small">
                            </a-button>
                        </div>

                        <el-input-number class="input-spacing" v-model="n_recs_per_model.unpersonalised" :min="0"
                            @change="saveInteraction('Model2: Input Number', 10-n_recs_per_model.personalised)"
                            :max=" 10" :step="1" :disabled="!inEdit" />
                        <el-dialog v-model="showDetails[1]" title="Model Details" width="30%" center>
                            <p>
                                Unpersonalised model: it does not take your personal preferences into account. All users
                                get similar recommendations.
                            </p>
                            <p>
                                It recommends the most viewed videos from each topic, choosing topics randomly. It shows
                                you which videos were the most popular among other users.
                            </p>
                        </el-dialog>
                    </div>


                </div>


            </a-col>

            <a-col :span="19" @mouseenter="isHovering[0] = true" @mouseleave="isHovering[0] = false">
                <div class="chart-container" :class="{ 'hover-border-topic': isHovering[0] }">

                    <!-- Exploit Title-->
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <div style="font-size: 20px; font-weight: bold;">Personalisation Slider</div>
                        <a-button @click="showDetails[2] = true; saveInteraction('Personalisation slider: Info button')"
                            shape="circle" :icon="h(InfoOutlined)" size="small">
                        </a-button>
                        <el-dialog v-model="showDetails[2]" title="How does it work?" width="30%" center>
                            <p>This slider defines how the personalised model makes recommendations.</p>
                            <p>The more exploration, the more your recommendations will focus on topics you have not
                                come across much yet.
                            </p>
                            <p> The more exploitation, the more your recommendations will focus on topics you seem to
                                like.
                            </p>

                        </el-dialog>
                    </div>

                    <!--  ExploitCoeffSlider -->
                    <div class="exloitative-slider-container">
                        <a-row justify="center" align="middle">
                            <a-col :span="4" offset="1">
                                <b>Pure explorative</b>
                            </a-col>
                            <a-col :span="10">
                                <el-slider v-model="exploit_coeff" :min="0" :max="1" :step="0.1" :disabled="!inEdit"
                                    show-stops :show-tooltip="false" @change="handleExploitCoeffChange"
                                    class="exploit-slider">
                                </el-slider>
                            </a-col>
                            <a-col :span="4" offset="1">
                                <b>Pure exploitative</b>
                            </a-col>
                        </a-row>
                    </div>

                    <!-- Topic Preferences -->
                    <div style="display: flex; align-items: center; gap: 10px; z-index: 100;">
                        <div style="font-size: 20px; font-weight: bold;">Topic Preferences</div>
                        <a-button style="z-index: 100;"
                            @click="showDetails[3] = true; saveInteraction('Topic Preferences: Info button')"
                            shape="circle" :icon="h(InfoOutlined)" size="small">
                        </a-button>
                        <el-dialog v-model="showDetails[3]" title="How does it work?" width="30%" center>
                            <p>
                                This menu visualises which video topics the system believes you like. The higher the
                                score of a topic, the more it will be featured in your exploitative recommendations.
                            </p>
                        </el-dialog>
                    </div>
                    <!-- Charts -->
                    <div style="width: 100%; height: 400px" ref="chartRef" class="chart"></div>

                    <!-- Sliders -->
                    <div class="sliders-container">
                        <a-row>
                            <a-col :span="12" v-for="(item, index) in topic_preferences" :key="item.id">
                                <a-row align="middle">
                                    <a-col :span="8">
                                        <a-tag :style="{ borderColor: chartColors[index] }"
                                            @mouseover="highlightChartSection(index)"
                                            @mouseleave="downplayChartSection(index)">
                                            {{ item.description }}
                                        </a-tag>
                                    </a-col>
                                    <a-col :span="8">
                                        <el-slider v-model="item.score" :min="0" :max="1" :step="0.01"
                                            @input="() => updateChartData(index)"
                                            @change="downplayChartSection(index); saveInteraction('Topic Preferences: Sliders', item.description)"
                                            :tipFormatter="value => `${(value * 100).toFixed(0)}%`"
                                            :style="{ color: chartColors[index] }" :disabled="!inEdit" />
                                    </a-col>
                                    <a-col :span="1" offset="1">
                                        {{ (item.score * 100).toFixed(0) }}%
                                    </a-col>
                                </a-row>
                            </a-col>
                        </a-row>
                    </div>
                </div>
            </a-col>
        </a-row>
    </div>
</template>

<script setup>
import { ref, onMounted, reactive, h, watch,defineExpose} from 'vue';
import * as echarts from 'echarts';
import apiClient from '@/config/apiClient';
import { InfoOutlined } from '@ant-design/icons-vue';
import {Edit} from '@element-plus/icons-vue';
import { ElNotification } from 'element-plus';
import globalState from '@/config/globalState';

const chartRef = ref(null);
const chartColors = ref([]);
const isHovering = ref([]);
const showDetails = ref([]);
const showSpin = ref(true);
const showEmpty = ref(false);
const inEdit = ref(false);

// User data from the API, which is used by resetting the user preferences
let userData = reactive({
    exploit_coeff: 0.5,
    topic_preferences: [],
    n_recs_per_model: { personalised: 5, unpersonalised: 5 },
});

// User data that can be edited
const exploit_coeff = ref(0.6);
const topic_preferences = ref([]);
const n_recs_per_model = ref({ personalised: 5, unpersonalised: 5 });

// Watchers that ensure the sum of the recommendations is always 10
watch(() => n_recs_per_model.value.personalised, (newValue) => {
    n_recs_per_model.value.unpersonalised = 10 - newValue;
});

watch(() => n_recs_per_model.value.unpersonalised, (newValue) => {
    n_recs_per_model.value.personalised = 10 - newValue;
});


// Load user data and update the chart
const refreshUserProfile = async () => {

    // Show the loading spinner and hide the empty message
    showSpin.value = true;
    showEmpty.value = false;

    // Fetch the user data from the API
    const fetchedUserData = await apiClient.getUser();

    // Update the user data
    userData = fetchedUserData;
    exploit_coeff.value = userData.exploit_coeff;
    topic_preferences.value = JSON.parse(JSON.stringify(userData.topic_preferences));
    n_recs_per_model.value = JSON.parse(JSON.stringify(userData.n_recs_per_model));
   
    // Update the chart
    chartInstance = echarts.init(chartRef.value);
    const data = topic_preferences.value.map(pref => ({
        name: pref.description,
        value: pref.score * 100,
    }));
    const option = {
        tooltip: {
            trigger: 'item',
            formatter: function (params) {
                const topic = topic_preferences.value.find(pref => pref.description === params.name);
                if (topic && topic.id !== -1) {
                    const imgPath = require(`@/assets/topics/topic_${topic.id}_wordcloud.png`);
                    const percentage = params.percent.toFixed(2);
                    return `<b>${topic.description}: ${percentage}%</b><br/><img src="${imgPath}" alt="${topic.description}" style="width: 200px; height: auto;"/>`;

                }
                return '<b>' + params.name + '</b>: ' + params.percent + '%';
            }
        },
        series: [
            {
                type: 'pie',
                radius: ['20%', '50%'],
                data: data,
                itemStyle: {
                    borderRadius: 0,
                    borderColor: '#fff',
                    borderWidth: 2
                },
                label: {
                    show: true,
                    position: 'outside',
                    formatter: params => {
                        const percentage = params.percent.toFixed(2);
                        return `{a|${params.name}: ${percentage}%}`;
                    },
                    rich: {
                        a: {
                            backgroundColor: '#f0f0f0',
                            borderColor: 'auto',
                            borderWidth: 1,
                            borderRadius: 4,
                            padding: [3, 4],
                            color: '#000',
                            fontSize: 14,
                            lineHeight: 22,
                            align: 'center',
                        }
                    }
                },
                emphasis: {
                    itemStyle: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    },
                    label: {
                        show: true,
                        backgroundColor: 'auto',
                        padding: [2, 3],
                        borderRadius: 4,
                    }
                }
            }
        ]
    };
    chartInstance.setOption(option);
    chartInstance = echarts.init(chartRef.value);

    // Fetch the chart colors to color the tags in the sliders
    chartColors.value = chartInstance.getOption().color;
    showSpin.value = false;
};
defineExpose({
    refreshUserProfile
})

const saveInteraction = (component, value = "") => {
    const interaction = {
        page: 'Profile',
        component: component,
        componentValue: value,
    };
    apiClient.submitInteraction(interaction);
};

const updateUserPreferences = async () => {
    try {
        userData.exploit_coeff = exploit_coeff.value;
        userData.topic_preferences = JSON.parse(JSON.stringify(topic_preferences.value));
        userData.n_recs_per_model = JSON.parse(JSON.stringify(n_recs_per_model.value));
        inEdit.value = false;
        apiClient.updateUser(userData);
        
    } catch (error) {
        console.error('Error updating user preferences:', error);
    }
};

// Reset the user preferences to the original values
const resetUserPreferences = () => {

    exploit_coeff.value = userData.exploit_coeff;
    topic_preferences.value = JSON.parse(JSON.stringify(userData.topic_preferences));
    n_recs_per_model.value = JSON.parse(JSON.stringify(userData.n_recs_per_model));

    const updatedData = topic_preferences.value.map((pref) => ({
        name: pref.description,
        value: pref.score * 100,
        selected: false,
    }));

    const updatedOption = {
        series: [{
            data: updatedData,
        }],
    };

    if (chartInstance) {
        chartInstance.setOption(updatedOption);
    }
    inEdit.value = false;
};

let chartInstance = null;
const updateChartData = (id) => {
    const updatedData = topic_preferences.value.map((pref, index) => ({
        name: pref.description,
        value: pref.score * 100,
        selected: index == id,
    }));

    const updatedOption = {
        series: [{
            data: updatedData,
        }],
    };

    if (chartInstance) {
        chartInstance.setOption(updatedOption);
        const temp = updatedOption.series[0];
        for (let i = 0; i < temp.data.length; i++) {
            if (temp.data[i].selected) {
                highlightChartSection(i, false);
            }
        }
    }

    var sum = 0;
    for (var i = 0; i < topic_preferences.value.length; i++) {
        sum += topic_preferences.value[i].score;
    }
    for (var j = 0; j < topic_preferences.value.length; j++) {
        topic_preferences.value[j].score = topic_preferences.value[j].score / sum;
    }
};


const highlightChartSection = (index, showTip = true) => {
    if (chartInstance) {
        chartInstance.dispatchAction({
            type: 'highlight',
            seriesIndex: 0,
            dataIndex: index
        });
        if (showTip) {
            chartInstance.dispatchAction({
                type: 'showTip',
                seriesIndex: 0,
                dataIndex: index
            });
        }
    }
};


const downplayChartSection = (index) => {
    if (chartInstance) {
        chartInstance.dispatchAction({
            type: 'downplay',
            seriesIndex: 0,
            dataIndex: index
        });
        chartInstance.dispatchAction({
             type: 'showTip',
             seriesIndex: 0,
             dataIndex: index
        });
        chartInstance.dispatchAction({
            type: 'hideTip'
        });
        
    }
};

const handleExploitCoeffChange = (value) => {
    saveInteraction('Personalisation slider: Slider', value);
};

onMounted(async () => {
    if (globalState.userId === null) {
        ElNotification({
            title: 'Error',
            message: 'Please log in first to see your profile',
            type: 'error',
            duration: 2000,
        });
        showEmpty.value = true;
        showSpin.value = false;
        return;
    }
    showEmpty.value = false;
    try {
        await refreshUserProfile();
    } catch (error) {
        console.error('Error loading user profile:', error);
    }
});
</script>

<style scoped>
.page-container {
    margin: -20px;
    background-color: #f0f2f5;
    min-height: 92vh;
    box-sizing: border-box;

}

.chart {
    margin-top: -70px;
    margin-bottom: 10px;
}

.model-container {
    background-color: #fff;
    border-radius: 8px;
    border: 3px solid #f0f0f0;
    margin-top: 10px;
    margin-bottom: 10px;
    margin-left: 10px;
    padding: 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.model-slider-container {
    background-color: #fff;
    border-radius: 8px;
    border: 3px dashed #215f9900 !important;
    margin: 5px;
    padding: 15px;
}


.chart-container {
    background-color: #fff;
    border-radius: 8px;
    border: 3px solid #f0f0f0;
    margin: 10px;
    padding: 15px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.hover-border-topic {
    border: 3px dashed #215f99 !important;
}

.input-spacing {
    max-width: 90vh;
    margin-top: 5px;
    display: center;
}
.exloitative-slider-container{
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    margin: 20px;
    padding: 20px;
}
.exloitative-slider-container .ant-slider {
    width: 80%;
    margin: 20px auto;
}

.exloitative-slider-container .ant-slider-mark-text {
    white-space: nowrap;
}

.spin-overlay {
    position: fixed;
    height: 100vh;
    border: 100px;
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgba(255, 255, 255, 0.8);
    z-index: 200;
}

.button-container {
    width: 100%;
    display: flex;
    justify-content: center;
    gap: 10px;
}

.button-container .el-button {
    width: 80%;
    display: block;
}

.sliders-container {
    margin-left: 20px;
    margin-right: 20px;
}


.exploit-slider .el-slider_runway {
    height: 32px;
    margin-top: 0;
    margin-bottom: 0 !important;
    background-color: #a62fc1 !important;
    border: 1px solid #DCDFE6;
    
}

</style>
