<template>
    <div class="navbar-pill-wrapper">
        <v-btn text class="navbar-pill" v-bind:class="{ active: isActiveTab() }" @click="showCompound">
            {{compound}}
        </v-btn>
        <span class="close-icon" @click="close">
            <i class="fa fa-times" aria-hidden="true"></i>
        </span>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import Component from 'vue-class-component';
import { Prop } from 'vue-property-decorator';
import RakUtil from '../util';

@Component
export default class NavbarPill extends Vue {

    @Prop({ required: true })
    compound: string;

    close(e: MouseEvent) {
        this.$emit('close', this.compound);
    }

    beforeDestroy() {
        const closeButton: Element = this.$el.querySelector('.close-icon')!;
        closeButton.removeEventListener('mouseover', this.onCloseButtonMouseOver);
        closeButton.removeEventListener('mouseout', this.onCloseButtonMouseOut);
    }

    created() {
        this.onCloseButtonMouseOver = this.onCloseButtonMouseOver.bind(this);
        this.onCloseButtonMouseOut = this.onCloseButtonMouseOut.bind(this);
    }

    isActiveTab(): boolean {
        return RakUtil.isActiveTab(this.$route, `/compound/${this.compound}`);
    }

    mounted() {
        // We must programmatically change style of "parent" div when close button
        // is armed unfortunately.
        const closeButton: Element = this.$el.querySelector('.close-icon')!;
        closeButton.addEventListener('mouseover', this.onCloseButtonMouseOver);
        closeButton.addEventListener('mouseout', this.onCloseButtonMouseOut);
    }

    onCloseButtonMouseOver() {
        const elem: HTMLElement = this.$el.getElementsByClassName('navbar-pill')[0] as HTMLElement;
        elem.classList.add('closeButtonArmed');
    }

    onCloseButtonMouseOut() {
        const elem: HTMLElement = this.$el.getElementsByClassName('navbar-pill')[0] as HTMLElement;
        elem.classList.remove('closeButtonArmed');
    }

    showCompound() {
        this.$emit('showCompoundDetails', this.compound);
    }
}
</script>

<style lang="less">
@import '../../styles/app-variables';
@close-icon-color: gray;

.navbar-pill-wrapper {

    height: inherit;
    position: relative;

    /* pill styles are essentially copied from ui inverted menu */
    .navbar-pill {

        text-overflow: ellipsis;
        overflow: hidden;
        white-space: nowrap;
        height: inherit !important;

        &:hover, &.active {
            background: rgba(255, 255, 255, 0.15) !important;
        }

        &.closeButtonArmed { // Hovering over the "close" icon
            background: rgba(255, 255, 255, 0.3) !important;
        }
    }

    &:hover .close-icon {
        opacity: 1;
        color: lighten(@close-icon-color, 25%);
    }

    .close-icon {

        transition: color @transition-time, opacity @transition-time;

        cursor: pointer;
        opacity: 0;
        color: @close-icon-color;
        font-size: 1.3rem;
        line-height: initial;
        position: absolute;
        top: 3px;
        right: 3px;

        &:hover {
            color: white;
        }
    }
}
</style>
