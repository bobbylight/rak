<template>
    <v-card-title>
        <div class="title-content">
            <div>
                <div>{{title}}</div>
                <div class="actionable-card-subtitle text--secondary" v-if="subTitle">{{subTitle}}</div>
            </div>
            <div class="admin-card-button-area">
                <slot></slot>
            </div>
        </div>
    </v-card-title>
</template>

<script lang="ts">
import Vue from 'vue';
import Component from 'vue-class-component';
import { Prop } from 'vue-property-decorator';

@Component
export default class ActionableCardTitle extends Vue {

    @Prop({ required: true })
    private readonly title: string;

    @Prop()
    private readonly subTitle: string | undefined;
}
</script>

<style lang="less">
.title-content {

    position: relative;
    width: 100%;

    .actionable-card-subtitle {
        font-size: initial;
    }

    .admin-card-button-area {

        position: absolute;
        top: 0;
        right: 0;

        .stats-filter-field {
            margin-right: 1rem;
        }
    }
}
</style>
