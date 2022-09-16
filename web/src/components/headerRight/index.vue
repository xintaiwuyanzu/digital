<template>
  <section>
    <portal-system/>
    <theme-select/>
    <full-screen/>
    <el-tooltip content="返回">
      <section @click="$router.back()" class="top_icon">
        <icon icon="arrow-left" style="width: 20px"/>
      </section>
    </el-tooltip>
    <person/>
    <logout/>
  </section>
</template>
<script>
import Logout from './logout'
import Person from './person'
import Home from './home'
import themeSelect from '@dr/framework/src/components/headerRight/themeSelect'
import fullScreen from '@dr/framework/src/components/headerRight/fullScreen'
import sysSelect from '@dr/framework/src/components/headerRight/sysSelect'
import portalSystem from "./portalSystem";

export default {
  components: {Logout, Person, Home, themeSelect, fullScreen, sysSelect, portalSystem}
}
</script>
<style>
.header-right > * {
  display: inline-block;
}
</style>