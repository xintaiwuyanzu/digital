<template>
    <el-tree highlight-current
             v-loading="loading"
             :data="categorys"
             accordion
             ref="tree"
             @node-click="n=>checkMethod(n)"
             default-expand-all
             node-key="id"
             :show-checkbox="showCheck">
    </el-tree>
<!--  @node-click="n=>$emit('check',n)"-->
</template>
<script>
  import abstractFond from "./fondTree/abstractFond";

  export default {
        extends: abstractFond,
        watch: {
            fondId() {
                this.loadCategory()
            },
        },
        data() {
            return {
                //所有门类
                categorys: [],
                //当前选中门类
                currentCategory: {}
            }
        },
        methods: {
            async loadCategory() {
                if (this.fondId) {
                    this.loading = true
                    const data = await this.$post("category/categoryTree", {group: this.fondId})
                    if (data.data.success) {
                        this.categorys = data.data.data
                      if (this.categorys.length > 0) {
                        this.currentCategory = this.categorys[0]
                        this.checkMethod(this.currentCategory);
                      }
                    }
                    this.loading = false
                }
            },
            /**
             * 获取选中的节点
             * @returns {D[]}
             */
            getCheckedNodes() {
                return this.$refs.tree.getCheckedNodes();
            },
            /**
             * 获取选中的Id
             * @returns {D[]}
             */
            getCheckedKeys() {
                return this.$refs.tree.getCheckedKeys()
            },
            $init() {
                this.loadCategory()
            },
          checkMethod(n){
               this.$emit('check',n);
          }
        },

  }
</script>
