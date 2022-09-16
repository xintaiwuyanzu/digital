<template>
    <section>
        <div v-if="row.disassembly_tagging === '1' ">
            <el-badge is-dot class="item">
                <el-link type="primary" @click="taggingDialog" v-show="!this.parentIndex">标记</el-link>
            </el-badge>
        </div>
        <div v-else>
            <el-badge class="item">
                <el-link type="primary" @click="taggingDialog" v-show="!this.parentIndex">标记</el-link>
            </el-badge>
        </div>
        <!--  <el-link type="primary" @click="taggingDialog" v-show="!this.parentIndex">标记</el-link>-->
        <el-dialog title="批注信息" :visible.sync="dialogsplitWsTagging"
                   width="60%"
                   center>
            <el-descriptions title="批注信息">
                <template slot="extra">
                    <el-button type="primary" v-if="taggingDataSelect==''" size="small"
                               @click="dialogTagging('添加批注')">添加
                    </el-button>
                    <el-button type="primary" v-if="taggingDataSelect.wssplitTaggingCondition=='1'" size="small"
                               @click="dialogTagging('修改批注')">修改
                    </el-button>
                    <el-button type="danger" v-if="taggingDataSelect.wssplitTaggingCondition=='1'" size="small"
                               @click="taggingDelet">删除
                    </el-button>
                </template>
                <el-descriptions-item label="批注">
                    <el-tag size="small">{{ taggingDataSelect.noteName }}</el-tag>
                </el-descriptions-item>
                <el-descriptions-item label="备注">
                    {{ taggingDataSelect.note }}
                </el-descriptions-item>
            </el-descriptions>
        </el-dialog>
        <el-dialog :title="dialogTaggingAddName" :visible.sync="dialogTaggingAdd"
                   width="60%"
                   center
                   @close="cancel()">
            <el-form ref="formTagging" :model="formTagging" label-width="80px" :rules="rulesTagging">
                <el-form-item label="批注名称" prop="noteName">
                    <el-input v-model="formTagging.noteName"></el-input>
                </el-form-item>
                <el-form-item label="批注详情" prop="desc">
                    <el-input type="textarea" v-model="formTagging.desc"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button type="primary" @click="taggingAdd(type)">{{ dialogTaggingAddName }}</el-button>
                    <el-button @click="cancel">取消</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
    </section>
</template>

<script>
import abstractColumnComponent from "./abstractColumnComponent";

export default {
    extends: abstractColumnComponent,
    name: 'splitWsTagging',
    data() {
        return {
            splitWsTagging: true,
            dialogsplitWsTagging: false,
            dialogTaggingAdd: false,
            taggingDataSelect: '',
            dialogTaggingAddName: '',
            type: '',
            formTagging: {
                noteName: '',
                desc: '',
            },
            rulesTagging: {
                noteName: [
                    {required: true, message: '请输入批注名称', trigger: 'blur'},
                ],
            },
            //disassembly_tagging: '',

        }
    },
    methods: {
        /*查询*/
        taggingDialog() {
            this.$http.post("/wsSplit/splitWsTaggingSelect", {
                archivesId: this.row.id
            }).then(({data}) => {
                if (data.success) {
                    if (data.data == null) {
                        this.taggingDataSelect = ""
                    } else {
                        this.taggingDataSelect = data.data
                    }
                    this.dialogsplitWsTagging = true
                } else {
                    this.$message.error(data.message);
                }
            }).catch()
        },
        dialogTagging(dialogTaggingAddName) {
            this.dialogTaggingAddName = dialogTaggingAddName
            if (dialogTaggingAddName === "添加批注") {
                this.type = 'splitWsTagging'
            } else {
                this.type = 'splitWsTaggingUpdate'
            }

            this.dialogTaggingAdd = true
        },
        /*添加&&修改*/
        taggingAdd(type) {
            this.$refs.formTagging.validate((valid) => {
                if (valid) {
                    this.$http.post("/wsSplit/" + type, Object.assign(this.taggingDataSelect,
                        {
                            formDefinitionId: this.formId,
                            categoryId: this.fond.registerId,
                            archivesId: this.row.id,
                            noteName: this.formTagging.noteName,
                            note: this.formTagging.desc,
                        })).then(({data}) => {
                        if (data.success) {
                            this.$message.success("操作成功")
                            this.taggingDialog()
                            this.$refs['formTagging'].resetFields()
                            this.dialogTaggingAdd = false
                        } else {
                            this.$message.error(data.message);
                        }
                    })
                }
            });
        },
        /*删除*/
        taggingDelet() {
            this.$http.post("/wsSplit/splitWsTaggingDelete", {
                formDefinitionId: this.formId,
                categoryId: this.fond.registerId,
                archivesId: this.row.id,
                noteName: this.formTagging.noteName,
                note: this.formTagging.desc,
            }).then(({data}) => {
                if (data.success) {
                    this.$message.success("删除成功")
                    this.taggingDialog()
                } else {
                    this.$message.error(data.message);
                }
            }).catch()
        },
        // 点击取消时关闭dialog并且清空form表格的数据
        cancel() {
            // 重置form表单
            this.$refs['formTagging'].resetFields()
            // 关闭dialog
            this.dialogTaggingAdd = false
        },
        loadData() {
            this.eventBus.$emit("loadData")
        }
    },
    /*mounted() {
        console.log(this.$el)
        //this.disassembly_tagging = this.row.disassembly_tagging
    }*/
}
</script>
