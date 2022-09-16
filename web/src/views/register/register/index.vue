<script src="receiveComponents.js"></script>
<script type="text/jsx">
    import archiveLib from "../../lib/archiveLib";
    import {columnParts, headerParts} from "./receiveComponents";

    const lib = archiveLib({type: 'RECEIVE', headerParts, columnParts}, '1')
    export default {
        render() {
            return <lib/>
        }
    }
</script>
