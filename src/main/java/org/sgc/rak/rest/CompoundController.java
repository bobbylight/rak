package org.sgc.rak.rest;

import org.apache.commons.lang3.StringUtils;
import org.sgc.rak.exceptions.InternalServerErrorException;
import org.sgc.rak.exceptions.NotFoundException;
import org.sgc.rak.i18n.Messages;
import org.sgc.rak.model.Compound;
import org.sgc.rak.reps.PagedDataRep;
import org.sgc.rak.services.CompoundService;
import org.sgc.rak.util.ImageTranscoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * REST API for compound information.
 */
@RestController
@RequestMapping("/api/compounds")
class CompoundController {

    private final CompoundService compoundService;
    private final ImageTranscoder imageTranscoder;
    private final Messages messages;

    private static final String MEDIA_TYPE_SVG = "image/svg+xml";

    @Autowired
    CompoundController(CompoundService compoundService, ImageTranscoder imageTranscoder, Messages messages) {
        this.compoundService = compoundService;
        this.imageTranscoder = imageTranscoder;
        this.messages = messages;
    }

    /**
     * Returns information on a specific compound.
     *
     * @param compoundName The compound name, ignoring case.
     * @return Information on the compound.
     */
    @GetMapping(path = "/{compoundName}")
    Compound getCompound(@PathVariable String compoundName) {

        Compound compound = compoundService.getCompound(compoundName);
        if (compound == null) {
            throw new NotFoundException(messages.get("error.noSuchCompound", compoundName));
        }
        return compound;
    }

    /**
     * Returns compound information, possibly filtered.
     *
     * @param compound A part of a compound name.  If specified, only compounds
     *        whose name contains this substring (ignoring case) will be returned.
     * @param pageInfo How to sort the data and what page of the data to return.
     * @return The list of compounds.
     */
    @GetMapping
    PagedDataRep<Compound> getCompounds(@RequestParam(required = false) String compound,
                                @RequestParam(required = false) String kinase,
                                @RequestParam(required = false) Double activity,
                                @RequestParam(required = false) Double kd,
                                @SortDefault("compoundName") Pageable pageInfo) {

        Page<Compound> page;

        if (StringUtils.isNotBlank(compound)) {
            page = compoundService.getCompoundsByCompoundName(compound, pageInfo);
        }
        else if (StringUtils.isNotBlank(kinase) && activity != null) {
            page = compoundService.getCompoundsByKinaseAndActivity(kinase, activity, pageInfo);
        }
        else if (StringUtils.isNotBlank(kinase) && kd != null) {
            page = compoundService.getCompoundsByKinaseAndKd(kinase, kd, pageInfo);
        }
        else {
            page = compoundService.getCompounds(pageInfo);
        }

        long start = page.getNumber() * pageInfo.getPageSize();
        long total = page.getTotalElements();
        return new PagedDataRep<>(page.getContent(), start, total);
    }

    /**
     * Returns the image for a compound as a PNG file.<p>
     * NOTE: For requests that don't explicitly specify {@code image/svg+xml} or {@code image/png}, we rely on
     * the presence of {@code width} and {@code height} request parameters to decide what image type to return.
     * This does mean that PNG requests require a specified width and height.  This seems to be the only way to handle
     * the fact that the {@code img} tag in browsers usually does not include SVG in its {@code Accept} header, but we
     * want to default to that image type.<p>
     * We could have different endpoints for PNG vs. SVG, but that's not very REST-like either.
     *
     * @param compoundName A compound name.
     * @param width The width of the PNG file to create.  (Note this is a {@code float} due to a Batik quirk).
     * @param height The height of the PNG file to create.  (Note this is a {@code float} due to a Batik quirk).
     * @return The image for the compound, in PNG format.  If no image exists for a compound, a default
     *         image is returned.
     * @see #getCompoundImageAsSvg(String)
     */
    @GetMapping(path = "/images/{compoundName}", params = { "width", "height" }, produces = MediaType.IMAGE_PNG_VALUE)
    public Resource getCompoundImageAsPng(@PathVariable String compoundName,
                                          @RequestParam float width, @RequestParam float height) {

        Resource resource = getCompoundImageAsSvg(compoundName);

        byte[] bytes;
        try {
            bytes = imageTranscoder.svgToPng(compoundName, resource.getInputStream(), width, height);
        } catch (IOException ioe) {
            throw new InternalServerErrorException(messages.get("error.creatingImage"), ioe);
        }

        return new ByteArrayResource(bytes);
    }

    /**
     * Returns the image for a compound as an SVG file.
     *
     * @param compoundName A compound name.
     * @return The image for the compound, in SVG format.  If no image exists for a compound, a default
     *         image is returned.
     * @see #getCompoundImageAsPng(String, float, float)
     */
    @GetMapping(path = "/images/{compoundName}", produces = MEDIA_TYPE_SVG)
    public Resource getCompoundImageAsSvg(@PathVariable String compoundName) {
        Resource resource = new ClassPathResource("/static/img/smiles/" + compoundName + ".svg");
        if (!resource.exists()) {
            resource = new ClassPathResource("/static/img/molecule-unknown.svg");
        }
        return resource;
    }
}
