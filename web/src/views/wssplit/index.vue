<script type="text/jsx">
    import archiveLib from "../lib/archiveLib";
    import {columnParts, headerParts} from "./recheckComponents";

    const lib = archiveLib({type: 'WSSPLIT', headerParts, columnParts}, '1')
    export default {
        render() {
            return <lib/>
        }
    }
</script>
