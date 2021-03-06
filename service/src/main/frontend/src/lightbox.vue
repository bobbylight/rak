<template>
    <transition name="fade">
        <v-overlay class="lightbox v-overlay"
             :value="show"
             :dark="false"
             @click="hide">

            <div class="lightbox-content-wrapper elevation-1 pa-3"
                    @click.stop="">

                <div class="lightbox-close-icon" @click="hide">
                    <v-tooltip right>
                        <template v-slot:activator="{ on }">
                            <v-icon v-on="on">close</v-icon>
                        </template>
                        <span>Close</span>
                    </v-tooltip>
                </div>

                <section-header>{{title}}</section-header>
                <v-col cols="12" class="pa-0">
                    <img :src="image" height=450 style="width: 100%"
                         onerror="this.src = 'img/molecule-unknown.svg';">
                </v-col>

                <v-col cols="12" style="text-align: right">
                    <div style="display: inline-block">
                        <div class="lightbox-footer-content">
                            <div class="lightbox-save-label">
                                Save as:
                            </div>
                            <v-select
                                class="lightbox-save-select"
                                :items="downloadOptions"
                                v-model="imageFormat"
                                required
                                hide-details
                                ></v-select>
                            <v-btn text color="primary" class="lightbox-save-button" @click="saveImage">
                                Download
                            </v-btn>
                        </div>
                    </div>
                </v-col>
            </div>
        </v-overlay>
    </transition>
</template>

<script lang="ts">
import Vue from 'vue';
import Component from 'vue-class-component';
import { Prop, Watch } from 'vue-property-decorator';
import SectionHeader from './header.vue';
import restApi from './rest-api';

@Component({ components: { SectionHeader } })
export default class Lightbox extends Vue {

    @Prop()
    private readonly title: string;

    @Prop({ required: true })
    private readonly image: string;

    imageFormat: string = 'png500500';

    private readonly imageWidth: number = 100;

    private show: boolean = false;

    private readonly downloadOptions: any[] = [
        { text: 'PNG (200x200)', value: 'png200200' },
        { text: 'PNG (300x300)', value: 'png300300' },
        { text: 'PNG (500x500)', value: 'png500500' },
        { text: 'SVG', value: 'svg' }
    ];

    created() {
        console.log('Adding resize handler');
        window.addEventListener('resize', this.onResize);
    }

    destroyed() {
        console.log('Removing resize handler');
        window.removeEventListener('resize', this.onResize);
    }

    @Watch('image')
    private onImageChanged(newValue: string | undefined) {
        this.show = !!newValue;
        if (this.show) {
            document.addEventListener('keydown', this.keyHandler);
        }
    }

    private hide() {
        console.log('hidden!');
        this.show = false;
        this.$emit('hide');
    }

    private onResize() {
        // const screenWidth: number = Math.max(document.documentElement.clientWidth || 0, window.innerWidth || 0);
        // const horizontalSpacing: number = 100;
        // this.imageWidth = Math.min(600, screenWidth - horizontalSpacing);
        // console.log('imageWidth now: ' + this.imageWidth);
    }

    keyHandler(e: KeyboardEvent) {
        if (e.key === 'Escape') {
            document.removeEventListener('keydown', this.keyHandler);
            this.hide();
        }
    }

    saveImage() {

        let width: number | undefined;
        let height: number | undefined;

        switch (this.imageFormat) {
            case 'png200200':
                width = height = 200;
                break;
            case 'png300300':
                width = height = 300;
                break;
            case 'png500500':
                width = height = 500;
                break;
        }

        restApi.downloadCompoundImage(this.title, width, height);
    }
}
</script>

<style lang="less">
@import '../styles/app-variables';

.lightbox {

    // Note: We rely on background, opacity, and layout styles to come from
    // vuetify's v-overlay and v-overlay--active.  So yes, we're a little fragile.

    display: flex;
    justify-content: center;
    align-items: center;

    .lightbox-content-wrapper {

        position: relative;
        background: white;

        .lightbox-close-icon {

            position: absolute;
            top: 1rem;
            right: 1rem;
            cursor: pointer;

            color: gray;
            transition: color @transition-time;

            &:hover i {
                color: black;
            }
        }

        .lightbox-footer-content {

            display: flex;
            align-items: center;

            .lightbox-save-label {
                margin-right: 1rem;
            }

            .lightbox-save-select {
                min-width: 4rem;
                padding-top: 0;
                margin-top: 0;
            }

            .lightbox-save-button {
                // Fit more snugly into bottom-right
                margin: 0 0 0 4px;
            }
        }
    }
}
</style>
