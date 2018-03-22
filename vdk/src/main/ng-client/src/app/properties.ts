export interface Dk {
  id: number;
  name: string;
  name_en: string;
  code: string;
  url: string;
  version: string;
  android: string;
  ios: string;
  logo: string;
  library_url: string;
}

export interface Result {
  id: number;
  title: string;
  authors: Array<string>;
  publisher: Array<string>;
  table: Array<any>;
  row: Array<any>;
  source: Array<string>;
  signature: string;
  status: string;
  type: string;
  volume: string;
  number: number;
  year: string;
}

