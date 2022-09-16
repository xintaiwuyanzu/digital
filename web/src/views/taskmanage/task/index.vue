<script type="text/jsx">
    import archiveLib from "../../lib/archiveLib";
    import {columnParts, headerParts} from "./taskComponents";

    const lib = archiveLib({type: '', headerParts, columnParts}, '3')
    export default {
        render() {
            return <lib/>
        }
    }
</script>
