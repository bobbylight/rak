package org.sgc.rak.rest;

import org.sgc.rak.model.Compound;
import org.sgc.rak.model.CompoundCountPair;
import org.sgc.rak.reps.PagedDataRep;
import org.sgc.rak.services.CompoundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.SortDefault;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.annotation.security.RolesAllowed;

/**
 * Returns random tidbits about the data.  Accessible only by admins.
 */
@RestController
@RolesAllowed("ADMIN")
@RequestMapping(path = "/admin/api/stats")
public class StatController {

    private final CompoundService compoundService;

    @Autowired
    StatController(CompoundService compoundService) {
        this.compoundService = compoundService;
    }

    /**
     * Returns information about compounds that are missing activity profiles.
     *
     * @param pageInfo How to sort the data and what page of the data to return.
     * @return The list of compounds.
     */
    @GetMapping(path = "/compoundsMissingActivityProfiles",
        produces = { MediaType.APPLICATION_JSON_VALUE, "text/csv" })
    PagedDataRep<CompoundCountPair> getCompoundsMissingActivityProfiles(
            @SortDefault("compoundName") Pageable pageInfo) {
        Page<CompoundCountPair> page = compoundService.getCompoundsMissingActivityProfiles(pageInfo);
        return pageToPagedDataRep(page, pageInfo.getPageSize());
    }

    /**
     * Returns information about compounds that are missing activity profiles.
     *
     * @param pageInfo How to sort the data and what page of the data to return.
     * @return The list of compounds.
     */
    @GetMapping(path = "/compoundsMissingPublicationInfo", produces = { MediaType.APPLICATION_JSON_VALUE, "text/csv" })
    PagedDataRep<Compound> getCompoundsMissingPublicationInfo(@SortDefault("compoundName") Pageable pageInfo) {
        Page<Compound> page = compoundService.getCompoundsMissingPublicationInfo(pageInfo);
        return pageToPagedDataRep(page, pageInfo.getPageSize());
    }

    /**
     * Returns information about compounds that are hidden.
     *
     * @param pageInfo How to sort the data and what page of the data to return.
     * @return The list of compounds.
     */
    @GetMapping(path = "/hiddenCompounds", produces = { MediaType.APPLICATION_JSON_VALUE, "text/csv" })
    PagedDataRep<Compound> getHiddenCompounds(@SortDefault("compoundName") Pageable pageInfo) {
        Page<Compound> page = compoundService.getHiddenCompounds(pageInfo);
        return pageToPagedDataRep(page, pageInfo.getPageSize());
    }

    /**
     * Returns information about compounds without SMILES strings or s(10) values, possibly filtered.
     *
     * @param pageInfo How to sort the data and what page of the data to return.
     * @return The list of compounds.
     */
    @GetMapping(path = "/incompleteCompounds", produces = { MediaType.APPLICATION_JSON_VALUE, "text/csv" })
    PagedDataRep<Compound> getIncompleteCompounds(@SortDefault("compoundName") Pageable pageInfo) {
        Page<Compound> page = compoundService.getIncompleteCompounds(pageInfo);
        return pageToPagedDataRep(page, pageInfo.getPageSize());
    }

    private static <T> PagedDataRep<T> pageToPagedDataRep(Page<T> page, int pageSize) {
        long start = (long)page.getNumber() * pageSize; // Cast to appease FindBugs
        long total = page.getTotalElements();
        return new PagedDataRep<>(page.getContent(), start, total);
    }
}
