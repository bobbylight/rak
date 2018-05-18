-- Mock data for development and unit testing using H2.

INSERT INTO compound(compound_nm, chemotype, s_10, source, smiles, reference_1, reference_1_url) VALUES
  ('compoundA', 'chemotypeA', 0.1, NULL, NULL, 'Reference A', 'https://www.google.com'),
  ('compoundB', 'chemotypeB', 0.2, NULL, NULL, NULL, 'https://www.google.com'),
  ('compoundC', 'chemotypeC', 0.3, NULL, NULL, 'Reference C', NULL)
;

INSERT INTO kinase(id, discoverx_gene_symbol, entrez_gene_symbol) VALUES
  (1, 'discoverxA', 'entrezA'),
  (2, 'discoverxB', 'entrezB'),
  (3, 'discoverxC', 'entrezC')
;

INSERT INTO kinase_activity_profile(id, compound_nm, kinase, percent_control, compound_concentration, kd) VALUES
  (1, 'compoundA', 1, 0.8, 5, 0.4),
  (2, 'compoundB', 2, 0.9, 6, 0.3),
  (3, 'compoundC', 3, 1.0, 7, 0.2)
;
