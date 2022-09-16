<template>
    <iframe :src="`/web/viewer.html?file=/${src}`" width="100%" height="100%" frameborder="0"/>
</template>
<script>
    export default {
        name: "pdfViewer",
        props: {
            src: {type: String, required: true}
        }
    }
</script>

