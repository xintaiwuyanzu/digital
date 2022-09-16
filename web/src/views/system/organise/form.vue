<template>
    <el-dialog :visible.sync="edit" :title="(form.id?'编辑':'添加')+'机构'" width="50%" :close-on-click-modal="false">
        <el-form :model="form" :rules="rules" ref="form" label-width="100px" inline>
          <el-row>
            <el-col :span="12">
            <el-form-item label="机构名称" prop="organiseName" required>
              <el-input v-model="form.organiseName"/>
            </el-form-item>
            </el-col>
            <el-col :span="12">
            <el-form-item label="原机构名称" prop="organiseOldName">
              <el-input v-model="form.organiseOldName"/>
            </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <el-form-item label="机构代码" prop="organiseCode" required>
                <el-input v-model="form.organiseCode"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="机构类型" prop="organiseType">
                <select-dict type="org.type" v-model="form.organiseType" placeholder="请选择机构类型"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <el-form-item label="联系人名称" prop="concatName" required>
                <el-input v-model="form.concatName"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="联系电话" prop="phone">
                <el-input v-model="form.phone" @input.native="handleClick"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <el-form-item label="联系手机" prop="mobile">
                <el-input v-model="form.mobile" @input.native="handleClick"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="经度" prop="latitude">
                <el-input v-model="form.latitude"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <el-form-item label="纬度" prop="longitude">
                <el-input v-model="form.longitude"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="办公地点" prop="address">
                <el-input v-model="form.address"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <el-form-item label="简介" prop="summary">
                <el-input type="textarea" v-model="form.summary"/>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="排序" prop="order">
                <el-input v-model="form.order"/>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="12">
              <el-form-item label="状态" prop="status">
                <select-dict type="orgStatus" v-model="form.status" placeholder="请选择机构状态"/>
              </el-form-item>
            </el-col>

          </el-row>
        </el-form>
        <div slot="footer" class="dialog-footer">
            <el-button @click="cancel">取 消</el-button>
            <el-button type="primary" @click="saveForm">确 定</el-button>
        </div>
    </el-dialog>
</template>
<script>
    import fromMixin from '@dr/auto/lib/util/formMixin'

    export default {
        data () {
            return {
                dict:['org.type'],
                autoClose: true,
                form: {phone: '', mobile: '', organiseCode: '10'},
                rules: {
                  organiseName: [
                    {required: true,message: '请填写入机构名称', trigger: 'blur'}
                  ],
                  organiseCode: [
                    {required: true,message: '请填写入机构代码', trigger: 'blur'}
                  ],
                  concatName: [
                    {required: true,message: '请填写入联系人名称', trigger: 'blur'}
                  ],
                },
            }
        },
        methods: {
            handleClick () {
                this.$nextTick(() => {
                    this.form.phone = this.form.phone.replace(/[^\d]/g, '')
                    this.form.mobile = this.form.mobile.replace(/[^\d]/g, '')
                })
            },
            search () {
                if (this.$parent && this.$parent.loadMenus) {
                    this.$parent.loadMenus()
                }
            }
        },
        mixins: [fromMixin]
    }
</script>
