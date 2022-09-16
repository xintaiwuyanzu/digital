<template>
  <div ref="tuiImageEditor" style=" width: 100%;height: 100%;"></div>
</template>
<script>
import ImageEditor from 'tui-image-editor';

const editorDefaultOptions = {
  cssMaxWidth: 700,
  cssMaxHeight: 500
};
export default {
  data() {
    return {
      includeUIOptions: {}
    }
  },
  watch: {
    includeUIOptions: {
      handler(newValue, oldValue) {
      },
      deep: true
    }
  },
  props: {
    includeUi: {
      type: Boolean,
      default: true
    },
    options: {
      type: Object,
      default() {
        return editorDefaultOptions;
      }
    }
  },
  mounted() {
    let options = editorDefaultOptions;
    if (this.includeUi) {
      options = Object.assign(this.includeUIOptions, this.options);
    }
    this.editorInstance = new ImageEditor(this.$refs.tuiImageEditor, options);
    this.addEventListener();
  },
  destroyed() {
    Object.keys(this.$listeners).forEach(eventName => {
      this.editorInstance.off(eventName);
    });
    this.editorInstance.destroy();
    this.editorInstance = null;
  },
  methods: {
    childMethod(flag) {  //这个就是子组件的函数 参数是父组件调用时传过来的
      this.includeUIOptions = flag
    },
    addEventListener() {
      Object.keys(this.$listeners).forEach(eventName => {
        this.editorInstance.on(eventName, (...args) => this.$emit(eventName, ...args));
      });
    },
    getRootElement() {
      return this.$refs.tuiImageEditor;
    },
    invoke(methodName, ...args) {
      let result = null;
      if (this.editorInstance[methodName]) {
        result = this.editorInstance[methodName](...args);
      } else if (methodName.indexOf('.') > -1) {
        const func = this.getMethod(this.editorInstance, methodName);
        if (typeof func === 'function') {
          result = func(...args);
        }
      }
      return result;
    },
    getMethod(instance, methodName) {
      const {first, rest} = this.parseDotMethodName(methodName);
      const isInstance = (instance.constructor.name !== 'Object');
      const type = typeof instance[first];
      let obj;
      if (isInstance && type === 'function') {
        obj = instance[first].bind(instance);
      } else {
        obj = instance[first];
      }
      if (rest.length > 0) {
        return this.getMethod(obj, rest);
      }
      return obj;
    },
    parseDotMethodName(methodName) {
      const firstDotIdx = methodName.indexOf('.');
      let firstMethodName = methodName;
      let restMethodName = '';
      if (firstDotIdx > -1) {
        firstMethodName = methodName.substring(0, firstDotIdx);
        restMethodName = methodName.substring(firstDotIdx + 1, methodName.length);
      }
      return {
        first: firstMethodName,
        rest: restMethodName
      };
    },
  }
};
</script>
<style lang="scss">
.tui-image-editor-header-logo {
  display: none;
}
</style>
