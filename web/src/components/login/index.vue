<template>
    <div class="login">
        <div class="loginform">
            <div class="logo">
                <span style="font-size: 30px;padding-top: 10px;">{{ headerTitle }}</span>
            </div>
            <div class="form">
                <el-form>
                    <el-form-item>
                        <select-async clearable
                                      placeholder="请选择登录类型"
                                      :mapper="{bsp:'单点登录',default:'默认登陆'}"
                                      v-model="loginForm.loginType"/>
                    </el-form-item>
                    <el-form-item>
                        <el-input type="text"
                                  placeholder="请输入用户名" v-model="loginForm.username" :disabled="dataLoading"
                                  autofocus @keyup.enter.native="keyEnterSerch">
                        </el-input>
                    </el-form-item>
                    <el-form-item>
                        <el-input placeholder="请输入密码" ref="psw"
                                  v-model="loginForm.password" type="password" :disabled="dataLoading"/>
                    </el-form-item>
                    <slide v-on:login="login()" :key="componentKey" ref="slide"></slide>
                    <!--          <el-button class="loginBtn" @click="doLogin(loginForm)"> 登 录-->
                    <!--          </el-button>-->
                </el-form>
            </div>
        </div>
    </div>
</template>
<script>
    import AbstractLogin from "./AbstractLogin";
    import slide from "./slide";
    import util from "@dr/framework/src/components/login/util";
    import {Loading} from 'element-ui';

    export default {
        data() {
            return {
                componentKey: 0,
                sso: false
            }
        },
        components: {slide},
        extends: AbstractLogin,
        methods: {
            keyEnterSerch() {
                this.$refs.psw.focus()
            },
            login() {
                this.doLogin(this.loginForm).then(() => {
                    this.componentKey += 1
                })
            },
            back() {
                console.log(this.$route.query.token)
                if (this.loading) {
                    this.loading.close()
                }
                this.$message.error("单点登录失败！，返回上一页面")
                /*setTimeout(() => {
                    history.back()
                }, 2000)*/
            },
            async $init() {
                console.log(this.$route.query.token)
                //如果是访问登录页面，并且带有token参数，则拦截并且使用sso登录
                if (this.$route.query.token) {
                    try {
                        this.sso = true
                        this.loading = Loading.service({fullscreen: true, text: '单点登录中。。。'});
                        //如果首页是redirect跳转到login的
                        const {data} = await this.$post('ssoLogin/validate', {token: this.$route.query.token})
                        if (data.success) {
                            //更改存储的token
                            util.setToken(this.$route.query.token)
                            this.loading.close()
                            await this.$router.replace({path: '/main', query: {params: this.$route.query.params}})
                        } else {
                            this.back()
                        }
                    } catch (e) {
                        this.back()
                    }
                }
            }


        },
        computed: {
            headerTitle() {
                if (this.title) {
                    return this.title
                }
                return document.title
            }
        }
    }
</script>

<style scoped lang="scss">
    .login {
        overflow: auto;
        width: 100vw;
        height: 100vh;
        background-image: url("./bg.jpg");
        background-size: contain;
        display: flex;
        align-content: center;


        .loginform {
            margin: auto;
            width: 405px;
            background-color: #f5f7fa;
            border-radius: 8px;

            .loginBtn {
                width: 351px;
                height: 51px;
                background-image: linear-gradient(180deg,
                        #04c688 0%,
                        rgba(4, 198, 136, 0.58) 100%),
                linear-gradient(
                                #04c688,
                                #04c688);
                background-blend-mode: multiply,
                normal;
                border-radius: 4px;
                font-size: 22px;
                font-weight: normal;
                font-stretch: normal;
                letter-spacing: 33px;
                color: #ffffff;
            }

            .logo {
                width: 381px;
                height: 66px;
                text-align: center;
                margin: 0 auto;
                padding: 19px 0px;
                border-bottom: 1px solid #e3eaef;
            }

            .form {
                margin: 22px 27px;
            }

            .el-form-item--small.el-form-item {
                height: 48px;
                line-height: 48px;
                width: 350px;
                margin-bottom: 20px;
            }

            ::v-deep input {
                height: 48px !important;
                line-height: 48px !important;
            }

            .el-input__inner {
                height: 48px !important;
                line-height: 48px !important;
            }

            .sr {
                height: 48px;
                line-height: 48px;
                flex: 1;
                border: 1px solid #8798aa;
                background: #ffffff;
                padding-left: 22px;
                color: #4e5459;
                margin-bottom: 26px;
            }
        }
    }
</style>