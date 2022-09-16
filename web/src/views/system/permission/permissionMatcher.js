class MatcherItem {
    constructor(code) {
        const arr = code.split(':')
        this.key = arr[0].trim()
        this.value = new Set
        if (arr.length === 2) {
            arr[1].trim().split(',')
                .forEach(a => this.value.add(a))
        }
    }

    match(code) {
        return this.value.has(code)
    }
}

export default class PermissionMatcher {
    constructor(code, parts) {
        this.matchMap = new Map
        this.parts = parts
        if (code) {
            code.trim().split(';')
                .forEach(v => {
                    v = v.trim()
                    if (v) {
                        const item = new MatcherItem(v);
                        this.matchMap.set(item.key, item)
                    }
                })
        }
    }

    toString() {
        const arr = []
        const partsLength = this.parts.length
        this.matchMap.forEach(v => {
            if (v.value.size > 0) {
                arr.push([v.key, Array.from(v.value).join(',')].join(':'))
            } else if (partsLength === 0) {
                arr.push(v.key)
            }
        })
        return arr.join(';')
    }

    match(resourceId, part) {
        resourceId = resourceId.trim()
        const item = this.matchMap.get(resourceId)
        if (item) {
            return item.match(part)
        }
        return false
    }

    matchAll(resourceId) {
        if (this.parts.length === 0) {
            return !!this.matchMap.get(resourceId.trim())
        }
        for (const p in this.parts) {
            if (this.match(resourceId, this.parts[p].code)) {
                return false
            }
        }
        return true
    }

    add(resourceId, part) {
        resourceId = resourceId.trim()
        let item = this.matchMap.get(resourceId)
        if (!item) {
            item = new MatcherItem(resourceId)
            this.matchMap.set(item.key, item)
        }
        item.value.add(part.trim())
    }

    remove(resourceId, part) {
        resourceId = resourceId.trim()
        const item = this.matchMap.get(resourceId)
        if (item) {
            item.value.delete(part.trim())
        }
    }

    addAll(resourceId) {
        resourceId = resourceId.trim()
        let item = this.matchMap.get(resourceId)
        if (!item) {
            item = new MatcherItem(resourceId)
            this.matchMap.set(item.key, item)
        }
        for (const p in this.parts) {
            item.value.add(this.parts[p].code.trim())
        }
    }

    removeAll(resourceId) {
        this.matchMap.delete(resourceId.trim())
    }

    toggleAll(id, t) {
        t ? this.addAll(id) : this.removeAll(id)
    }

    toggle(id, t, part) {
        t ? this.add(id, part) : this.remove(id, part)
    }

}
