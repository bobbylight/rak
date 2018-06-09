export interface ActivityProfile {
    id: number;
    compoundName: string;
    kinase: Kinase;
    percentControl: number;
    compoundConcentration: number;
    kd: number;
}

export interface BlogPost {
    id?: number;
    title: string;
    body: string;
    author?: string;
    createDate?: string;
}

export interface Compound {
    compoundName: string;
    chemotype: string;
    s10: string;
    smiles?: string;
    source?: string;
    primaryReference?: string;
    primaryReferenceUrl?: string;
}

export interface ErrorResponse {
    statusCode: number;
    message: string;
}

export interface Feedback {
    id?: number;
    email?: string;
    ipAddress?: string;
    title: string;
    body: string;
    createDate?: string;
}

export interface FieldStatus {
    fieldName: string;
    oldValue?: any;
    newValue: any;
}

export interface Kinase {
    id: number;
    discoverxGeneSymbol: string;
    entrezGeneSymbol: string;
}

export interface ObjectImportRep {
    fieldStatuses: FieldStatus[][];
}

export interface PagedDataRep<T> {
    data: T[];
    start: number;
    count: number;
    total: number;
}

export interface Partner {
    id: number;
    name: string;
    url: string;
}

export interface RakState {
    user: string;
    lightboxImage: string | null;
    lightboxTitle: string | undefined;
    filters: SearchFilter;
    lastAdminRouteName: string;
}

export interface SearchFilter {
    inhibitor: string;
    kinase: string;
    activity: any;
    kd: any;
    activityOrKd: SearchByKinaseSecondComponent;
}

export type SearchByKinaseSecondComponent = 'kd' | 'percentControl';

export interface UserRep {
    userName?: string;
    lightboxImage?: string;
}
