<script type="text/jsx">
import archiveLib from "../lib/archiveLib";
import {columnParts, headerParts} from "./overComponents";

const lib = archiveLib({type: 'OVER', headerParts, columnParts}, '1')
export default {
  render() {
    return <lib/>
  }
}
</script>