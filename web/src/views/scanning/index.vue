<script type="text/jsx">
    import archiveLib from "../lib/archiveLib";
    import {columnParts, headerParts} from "./scanningComponents";

    const lib = archiveLib({type: 'SCANNING', headerParts, columnParts}, '1')
    export default {
        render() {
            return <lib/>
        }
    }
</script>