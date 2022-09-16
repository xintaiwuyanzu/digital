<script type="text/jsx">
    import archiveLib from "../lib/archiveLib";
    import {columnParts, headerParts} from "./processingComponents";

    const lib = archiveLib({type: 'PROCESSING', headerParts, columnParts}, '1')
    export default {
        render() {
            return <lib/>
        }
    }
</script>