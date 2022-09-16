<script type="text/jsx">
    import archiveLib from "../lib/archiveLib";
    import {columnParts, headerParts} from "./volumesComponents";

    const lib = archiveLib({type: 'VOLUMES', headerParts, columnParts}, '1')
    export default {
        render() {
            return <lib/>
        }
    }
</script>