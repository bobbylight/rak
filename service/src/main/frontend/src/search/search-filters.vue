<template>
    <v-row>

        <v-col cols="12">
            <v-text-field type="text" label="Search by compound" class="search-field"
                          autocomplete="off" hide-details
                          v-model="filters.inhibitor"></v-text-field>
        </v-col>

        <v-col md="8">
            <lazy-dropdown v-model="filters.kinase"
                           id="kinase"
                           :label="kinaseDropdownLabel"
                           name="kinase" url="api/kinases"
                           :queryParams="kinaseQueryParams"
                           filterParamName="entrez"
                           responseLabelField="entrezGeneSymbol"
                           responseValueField="responseValueField"
                           :transform="kinaseResponseTransformer"></lazy-dropdown>
        </v-col>

        <v-col sm="4">
            <activity-or-kd-field></activity-or-kd-field>
        </v-col>
    </v-row>
</template>

<script lang="ts">
import Vue from 'vue';
import Component from 'vue-class-component';
import LazyDropdown from '../lazy-dropdown.vue';
import { Kinase, PagedDataRep } from '../rak';
import ActivityOrKdField from './activity-or-kd-field.vue';

@Component({ components: { ActivityOrKdField, LazyDropdown } })
export default class SearchFilters extends Vue {

    get filters() {
        return this.$store.state.filters;
    }

    get kinaseDropdownLabel(): string {
        return this.filters.activityOrKd === 'percentControl' ? 'Or by kinase and activity' : 'Or by kinase and Kd';
    }

    private readonly kinaseQueryParams: any = { size: 1000 };

    private kinaseResponseTransformer(response: PagedDataRep<Kinase>): any[] {
        const choices: any[] = response.data.map((kinase: Kinase) => {
            const entrez: string = kinase.entrezGeneSymbol;
            return { entrezGeneSymbol: entrez, responseValueField: entrez };
        });
        //choices.unshift({ entrezGeneSymbol: '', responseValueField: '' });
        return choices;
    }
}
</script>

<style lang="less">
.search-field {
    width: 100%;

    &.right-aligned {
        input {
            text-align: right;
        }
    }
}
.ui.input.search-field.error {
    .ui.label {
        border: 1px solid #e0b4b4; // Matches input's error border
    }
}
</style>
