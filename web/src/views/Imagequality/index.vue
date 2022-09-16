<script type="text/jsx">
    import archiveLib from "../lib/archiveLib";
    import {columnParts, headerParts} from "./qualityComponents";

    const lib = archiveLib({type: 'IMAGES', headerParts, columnParts}, '1')
    export default {
        render() {
            return <lib/>
        }
    }
</script>