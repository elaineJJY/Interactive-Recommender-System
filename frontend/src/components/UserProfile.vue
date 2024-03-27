<template>
    <div class="page-container">
        <a-spin size="large" class="spin-overlay" v-if="showSpin"></a-spin>
        <a-row align="top" justify="space-between">
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
                            Here, you can tailor your video feed by modifying the balance among various recommendation
                            models, shaping the selection of every 10-video batch to better match your interests.
                        </blockquote>
                    </a-typography-text>


                    <!-- Models -->
                    <!-- topic preferences -->
                    <div class="model-slider-container" @mouseenter="isHovering[0] = true"
                        @mouseleave="isHovering[0] = false" :class="{ 'hover-border-topic': isHovering[0] }">
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <div style="font-size: 16px; font-weight: bold;">Model: Topic Preferences</div>
                            <a-button @click="showDetails[0] = true" shape="circle" :icon="h(InfoOutlined)"
                                size="small">
                            </a-button>
                        </div>

                        <el-input-number class="input-spacing" v-model="n_recs_per_model.personalised" :min="0"
                            :max="10" :step="1" :disabled="!inEdit" />
                        <el-dialog v-model="showDetails[0]" title="Model Details" width="30%" center>
                            <p>XXXXXX</p>
                        </el-dialog>
                    </div>

                    <!-- top-popular -->
                    <div class="model-slider-container" @mouseenter="isHovering[1] = true"
                        @mouseleave="isHovering[1] = false" :class="{ 'hover-border-topic': isHovering[1] }">
                        <div style="display: flex; align-items: center; gap: 10px;">
                            <div style="font-size: 16px; font-weight: bold;">Model: Top-popular</div>
                            <a-button @click="showDetails[1] = true" shape="circle" :icon="h(InfoOutlined)"
                                size="small">
                            </a-button>
                        </div>

                        <el-input-number class="input-spacing" v-model="n_recs_per_model.unpersonalised" :min="0"
                            :max="10" :step="1" :disabled="!inEdit" />
                        <el-dialog v-model="showDetails[1]" title="Model Details" width="30%" center>
                            <p>XXXXXX</p>
                        </el-dialog>
                    </div>


                </div>


            </a-col>

            <a-col :span="19" @mouseenter="isHovering[0] = true" @mouseleave="isHovering[0] = false">
                <div class="chart-container" :class="{ 'hover-border-topic': isHovering[0] }">
                    <h2>Topic Preferences</h2>

                    <!-- Explorative Slider -->
                    <div class="custom-slider-container">
                        <a-row justify="center" align="middle">
                            <a-col :span="5">
                                <b>Pure exloitative</b>
                            </a-col>
                            <a-col :span="10">
                                <el-slider v-model="exploit_coeff" :min="0" :max="1" :step="0.1" :disabled="!inEdit"
                                    show-stops>
                                </el-slider>
                            </a-col>
                            <a-col :span="5">
                                <b>Pure explorative</b>
                            </a-col>
                        </a-row>
                    </div>
                    <!-- Charts -->
                    <div style="width: 100%; height: 400px" ref="chartRef" class="chart"></div>

                    <!-- Sliders -->
                    <div class="sliders-container">
                        <a-row>
                            <a-col :span="12" v-for="(item, index) in topic_preferences" :key="item.id">
                                <a-row align="middle">
                                    <a-col :span="14">
                                        <a-tag :style="{ borderColor: chartColors[index] }"
                                            @mouseover="highlightChartSection(index)"
                                            @mouseleave="downplayChartSection(index)">
                                            {{ item.description }}
                                        </a-tag>
                                    </a-col>
                                    <a-col :span="8">
                                        <el-slider v-model="item.score" :min="0" :max="1" :step="0.01"
                                            @input="() => updateChartData(index)"
                                            @change="() => downplayChartSection(index)"
                                            :tipFormatter="value => `${(value * 100).toFixed(0)}%`"
                                            :style="{ color: chartColors[index] }" :disabled="!inEdit" />
                                    </a-col>
                                    <a-col :span="1">
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
import { ref, onMounted, reactive, h, watch} from 'vue';
import * as echarts from 'echarts';
import apiClient from '@/config/apiClient';
import { InfoOutlined } from '@ant-design/icons-vue';
import {Edit} from '@element-plus/icons-vue';
import { ElNotification } from 'element-plus';
const chartRef = ref(null);
const chartColors = ref([]);
const isHovering = ref([]);
const showDetails = ref([]);
const showSpin = ref(true);
const inEdit = ref(false);
let userData = reactive({
    exploit_coeff: 0.5,
    topic_preferences: [],
    n_recs_per_model: { personalised: 5, unpersonalised: 5 },
    origin_other_topics: {},
});

const exploit_coeff = ref(0.5);
const topic_preferences = ref([]);
const n_recs_per_model = ref({ personalised: 5, unpersonalised: 5 });
const origin_other_topics = ref({});

watch(() => n_recs_per_model.value.personalised, (newValue) => {
    n_recs_per_model.value.unpersonalised = 10 - newValue;
});

watch(() => n_recs_per_model.value.unpersonalised, (newValue) => {
    n_recs_per_model.value.personalised = 10 - newValue;
});

const updateUserPreferences = async () => {
    try {
        userData.exploit_coeff = exploit_coeff.value;
        userData.topic_preferences = JSON.parse(JSON.stringify(topic_preferences.value));
        userData.n_recs_per_model = JSON.parse(JSON.stringify(n_recs_per_model.value));
        userData.origin_other_topics = JSON.parse(JSON.stringify(origin_other_topics.value));
        inEdit.value = false;
        apiClient.updateUser(userData);
        ElNotification({
            title: 'Success',
            message: 'User preferences updated successfully',
            type: 'success',
            duration: 2000,
        });
        
    } catch (error) {
        console.error('Error updating user preferences:', error);
    }
};

const resetUserPreferences = () => {

    exploit_coeff.value = userData.exploit_coeff;
    topic_preferences.value = JSON.parse(JSON.stringify(userData.topic_preferences));
    n_recs_per_model.value = JSON.parse(JSON.stringify(userData.n_recs_per_model));
    origin_other_topics.value = JSON.parse(JSON.stringify(userData.origin_other_topics));

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

onMounted(async () => {
    const fetchedUserData = await apiClient.getUser();
    showSpin.value = false;
    userData = fetchedUserData;
    
    exploit_coeff.value = userData.exploit_coeff;
    topic_preferences.value = JSON.parse(JSON.stringify(userData.topic_preferences));
    n_recs_per_model.value = JSON.parse(JSON.stringify(userData.n_recs_per_model));
    origin_other_topics.value = JSON.parse(JSON.stringify(userData.origin_other_topics));
   
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

    chartColors.value = chartInstance.getOption().color;

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
    margin-top: -60px;
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
    margin-left: 20px;
    margin-top: 10px;
}
.custom-slider-container{
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 8px;
    margin: 20px;
}
.custom-slider-container .ant-slider {
    width: 80%;
    margin: 20px auto;
}

.custom-slider-container .ant-slider-mark-text {
    white-space: nowrap;
}

.spin-overlay {
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    background: rgba(255, 255, 255, 0.8);
    z-index: 1000;
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
</style>
